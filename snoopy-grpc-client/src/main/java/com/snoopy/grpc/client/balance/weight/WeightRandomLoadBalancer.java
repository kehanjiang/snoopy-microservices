package com.snoopy.grpc.client.balance.weight;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.snoopy.grpc.base.constans.GrpcConstants;
import io.grpc.*;
import io.grpc.internal.GrpcAttributes;
import io.grpc.internal.ServiceConfigUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.grpc.ConnectivityState.*;
import static io.grpc.ConnectivityState.READY;

/**
 * @author :   kehanjiang
 * @date :   2021/12/10  16:00
 */
public class WeightRandomLoadBalancer extends LoadBalancer {
    @VisibleForTesting
    static final Attributes.Key<Ref<ConnectivityStateInfo>> STATE_INFO =
            Attributes.Key.create("state-info");
    // package-private to avoid synthetic access
    static final Attributes.Key<Ref<Subchannel>> STICKY_REF = Attributes.Key.create("sticky-ref");

    private final Helper helper;
    private final Map<EquivalentAddressGroup, Subchannel> subchannels =
            new HashMap<>();
    private final Random random;

    private ConnectivityState currentState;
    private WeightRandomPicker currentPicker = new EmptyPicker(EMPTY_OK);

    @Nullable
    private WeightRandomLoadBalancer.StickinessState stickinessState;

    WeightRandomLoadBalancer(Helper helper) {
        this.helper = checkNotNull(helper, "helper");
        this.random = new Random();
    }

    @Override
    public void handleResolvedAddresses(ResolvedAddresses resolvedAddresses) {
        List<EquivalentAddressGroup> servers = resolvedAddresses.getAddresses();
        Attributes attributes = resolvedAddresses.getAttributes();
//        Set<EquivalentAddressGroup> currentAddrs = subchannels.keySet();
        Map<EquivalentAddressGroup, EquivalentAddressGroup> latestAddrs = stripAttrs(servers);
//        Set<EquivalentAddressGroup> removedAddrs = setsDifference(currentAddrs, latestAddrs.keySet());

        Map<String, ?> serviceConfig = attributes.get(GrpcAttributes.NAME_RESOLVER_SERVICE_CONFIG);
        if (serviceConfig != null) {
            String stickinessMetadataKey =
                    ServiceConfigUtil.getStickinessMetadataKeyFromServiceConfig(serviceConfig);
            if (stickinessMetadataKey != null) {
                if (stickinessMetadataKey.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                    helper.getChannelLogger().log(
                            ChannelLogger.ChannelLogLevel.WARNING,
                            "Binary stickiness header is not supported. The header \"{0}\" will be ignored",
                            stickinessMetadataKey);
                } else if (stickinessState == null
                        || !stickinessState.key.name().equals(stickinessMetadataKey)) {
                    stickinessState = new StickinessState(stickinessMetadataKey);
                }
            }
        }
        //每次地址有变时，关掉subchannels内所有subchannel并清空重新创建，这样才能更新subchannel的attribute属性。
        for (Subchannel subchannel : subchannels.values()) {
            updateBalancingState();
            shutdownSubchannel(subchannel);
        }
        subchannels.clear();

        for (Map.Entry<EquivalentAddressGroup, EquivalentAddressGroup> latestEntry :
                latestAddrs.entrySet()) {
            EquivalentAddressGroup strippedAddressGroup = latestEntry.getKey();
            EquivalentAddressGroup originalAddressGroup = latestEntry.getValue();
//            Subchannel existingSubchannel = subchannels.get(strippedAddressGroup);
//            if (existingSubchannel != null) {
            // EAG's Attributes may have changed.
//                existingSubchannel.updateAddresses(Collections.singletonList(originalAddressGroup));
//                continue;
//            }
            // Create new subchannels for new addresses.

            // NB(lukaszx0): we don't merge `attributes` with `subchannelAttr` because subchannel
            // doesn't need them. They're describing the resolved server list but we're not taking
            // any action based on this information.
            Attributes.Builder subchannelAttrs = Attributes.newBuilder()
                    // NB(lukaszx0): because attributes are immutable we can't set new value for the key
                    // after creation but since we can mutate the values we leverage that and set
                    // AtomicReference which will allow mutating state info for given channel.
                    .set(STATE_INFO,
                            new Ref<>(ConnectivityStateInfo.forNonError(IDLE)));

            subchannelAttrs.set(GrpcConstants.WEIGHT_LIST_KEY, attributes.get(GrpcConstants.WEIGHT_LIST_KEY));

            Ref<Subchannel> stickyRef = null;
            if (stickinessState != null) {
                subchannelAttrs.set(STICKY_REF, stickyRef = new Ref<>(null));
            }

            final Subchannel subchannel = checkNotNull(
                    helper.createSubchannel(CreateSubchannelArgs.newBuilder()
                            .setAddresses(originalAddressGroup)
                            .setAttributes(subchannelAttrs.build())
                            .build()),
                    "subchannel");
            subchannel.start(new SubchannelStateListener() {
                @Override
                public void onSubchannelState(ConnectivityStateInfo state) {
                    processSubchannelState(subchannel, state);
                }
            });
            if (stickyRef != null) {
                stickyRef.value = subchannel;
            }
            subchannels.put(strippedAddressGroup, subchannel);
            subchannel.requestConnection();
        }

//        ArrayList<Subchannel> removedSubchannels = new ArrayList<>();
//        for (EquivalentAddressGroup addressGroup : removedAddrs) {
//            removedSubchannels.add(subchannels.remove(addressGroup));
//        }

        // Update the picker before shutting down the subchannels, to reduce the chance of the race
        // between picking a subchannel and shutting it down.
//        updateBalancingState();

        // Shutdown removed subchannels
//        for (Subchannel removedSubchannel : removedSubchannels) {
//            shutdownSubchannel(removedSubchannel);
//        }
    }

    @Override
    public void handleNameResolutionError(Status error) {
        // ready pickers aren't affected by status changes
        updateBalancingState(TRANSIENT_FAILURE,
                currentPicker instanceof ReadyPicker ? currentPicker : new EmptyPicker(error));
    }

    private void processSubchannelState(Subchannel subchannel, ConnectivityStateInfo stateInfo) {
        if (subchannels.get(stripAttrs(subchannel.getAddresses())) != subchannel) {
            return;
        }
        if (stateInfo.getState() == SHUTDOWN && stickinessState != null) {
            stickinessState.remove(subchannel);
        }
        if (stateInfo.getState() == IDLE) {
            subchannel.requestConnection();
        }
        getSubchannelStateInfoRef(subchannel).value = stateInfo;
        updateBalancingState();
    }

    private void shutdownSubchannel(Subchannel subchannel) {
        subchannel.shutdown();
        getSubchannelStateInfoRef(subchannel).value =
                ConnectivityStateInfo.forNonError(SHUTDOWN);
        if (stickinessState != null) {
            stickinessState.remove(subchannel);
        }
    }

    @Override
    public void shutdown() {
        for (Subchannel subchannel : getSubchannels()) {
            shutdownSubchannel(subchannel);
        }
    }

    private static final Status EMPTY_OK = Status.OK.withDescription("no subchannels ready");

    /**
     * Updates picker with the list of active subchannels (state == READY).
     */
    @SuppressWarnings("ReferenceEquality")
    private void updateBalancingState() {
        List<Subchannel> activeList = filterNonFailingSubchannels(getSubchannels());
        if (activeList.isEmpty()) {
            // No READY subchannels, determine aggregate state and error status
            boolean isConnecting = false;
            Status aggStatus = EMPTY_OK;
            for (Subchannel subchannel : getSubchannels()) {
                ConnectivityStateInfo stateInfo = getSubchannelStateInfoRef(subchannel).value;
                // This subchannel IDLE is not because of channel IDLE_TIMEOUT,
                // in which case LB is already shutdown.
                // RRLB will request connection immediately on subchannel IDLE.
                if (stateInfo.getState() == CONNECTING || stateInfo.getState() == IDLE) {
                    isConnecting = true;
                }
                if (aggStatus == EMPTY_OK || !aggStatus.isOk()) {
                    aggStatus = stateInfo.getStatus();
                }
            }
            updateBalancingState(isConnecting ? CONNECTING : TRANSIENT_FAILURE,
                    // If all subchannels are TRANSIENT_FAILURE, return the Status associated with
                    // an arbitrary subchannel, otherwise return OK.
                    new EmptyPicker(aggStatus));
        } else {
            // initialize the Picker to a random start index to ensure that a high frequency of Picker
            // churn does not skew subchannel selection.
            int startIndex = random.nextInt(activeList.size());
            updateBalancingState(READY, new ReadyPicker(activeList, subchannels, startIndex, stickinessState));
        }
    }

    private void updateBalancingState(ConnectivityState state, WeightRandomPicker picker) {
        if (state != currentState || !picker.isEquivalentTo(currentPicker)) {
            helper.updateBalancingState(state, picker);
            currentState = state;
            currentPicker = picker;
        }
    }

    /**
     * Filters out non-ready subchannels.
     */
    private static List<Subchannel> filterNonFailingSubchannels(
            Collection<Subchannel> subchannels) {
        List<Subchannel> readySubchannels = new ArrayList<>(subchannels.size());
        for (Subchannel subchannel : subchannels) {
            if (isReady(subchannel)) {
                readySubchannels.add(subchannel);
            }
        }
        return readySubchannels;
    }

    /**
     * Converts list of {@link EquivalentAddressGroup} to {@link EquivalentAddressGroup} set and
     * remove all attributes. The values are the original EAGs.
     */
    private static Map<EquivalentAddressGroup, EquivalentAddressGroup> stripAttrs(
            List<EquivalentAddressGroup> groupList) {
        Map<EquivalentAddressGroup, EquivalentAddressGroup> addrs = new HashMap<>(groupList.size() * 2);
        for (EquivalentAddressGroup group : groupList) {
            addrs.put(stripAttrs(group), group);
        }
        return addrs;
    }

    private static EquivalentAddressGroup stripAttrs(EquivalentAddressGroup eag) {
        return new EquivalentAddressGroup(eag.getAddresses());
    }

    @VisibleForTesting
    Collection<Subchannel> getSubchannels() {
        return subchannels.values();
    }

    private static Ref<ConnectivityStateInfo> getSubchannelStateInfoRef(
            Subchannel subchannel) {
        return checkNotNull(subchannel.getAttributes().get(STATE_INFO), "STATE_INFO");
    }

    // package-private to avoid synthetic access
    static boolean isReady(Subchannel subchannel) {
        return getSubchannelStateInfoRef(subchannel).value.getState() == READY;
    }

    private static <T> Set<T> setsDifference(Set<T> a, Set<T> b) {
        Set<T> aCopy = new HashSet<>(a);
        aCopy.removeAll(b);
        return aCopy;
    }

    Map<String, Ref<Subchannel>> getStickinessMapForTest() {
        if (stickinessState == null) {
            return null;
        }
        return stickinessState.stickinessMap;
    }

    /**
     * Holds stickiness related states: The stickiness key, a registry mapping stickiness values to
     * the associated Subchannel Ref, and a map from Subchannel to Subchannel Ref.
     */
    @VisibleForTesting
    static final class StickinessState {
        static final int MAX_ENTRIES = 1000;

        final Metadata.Key<String> key;
        final ConcurrentMap<String, Ref<Subchannel>> stickinessMap =
                new ConcurrentHashMap<>();

        final Queue<String> evictionQueue = new ConcurrentLinkedQueue<>();

        StickinessState(@Nonnull String stickinessKey) {
            this.key = Metadata.Key.of(stickinessKey, Metadata.ASCII_STRING_MARSHALLER);
        }

        /**
         * Returns the subchannel associated to the stickiness value if available in both the
         * registry and the round robin list, otherwise associates the given subchannel with the
         * stickiness key in the registry and returns the given subchannel.
         */
        @Nonnull
        Subchannel maybeRegister(
                String stickinessValue, @Nonnull Subchannel subchannel) {
            final Ref<Subchannel> newSubchannelRef = subchannel.getAttributes().get(STICKY_REF);
            while (true) {
                Ref<Subchannel> existingSubchannelRef =
                        stickinessMap.putIfAbsent(stickinessValue, newSubchannelRef);
                if (existingSubchannelRef == null) {
                    // new entry
                    addToEvictionQueue(stickinessValue);
                    return subchannel;
                } else {
                    // existing entry
                    Subchannel existingSubchannel = existingSubchannelRef.value;
                    if (existingSubchannel != null && isReady(existingSubchannel)) {
                        return existingSubchannel;
                    }
                }
                // existingSubchannelRef is not null but no longer valid, replace it
                if (stickinessMap.replace(stickinessValue, existingSubchannelRef, newSubchannelRef)) {
                    return subchannel;
                }
                // another thread concurrently removed or updated the entry, try again
            }
        }

        private void addToEvictionQueue(String value) {
            String oldValue;
            while (stickinessMap.size() >= MAX_ENTRIES && (oldValue = evictionQueue.poll()) != null) {
                stickinessMap.remove(oldValue);
            }
            evictionQueue.add(value);
        }

        /**
         * Unregister the subchannel from StickinessState.
         */
        void remove(Subchannel subchannel) {
            subchannel.getAttributes().get(STICKY_REF).value = null;
        }

        /**
         * Gets the subchannel associated with the stickiness value if there is.
         */
        @Nullable
        Subchannel getSubchannel(String stickinessValue) {
            Ref<Subchannel> subchannelRef = stickinessMap.get(stickinessValue);
            if (subchannelRef != null) {
                return subchannelRef.value;
            }
            return null;
        }
    }

    // Only subclasses are ReadyPicker or EmptyPicker
    private abstract static class WeightRandomPicker extends SubchannelPicker {
        abstract boolean isEquivalentTo(WeightRandomPicker picker);
    }

    @VisibleForTesting
    static final class ReadyPicker extends WeightRandomPicker {
        private static final AtomicIntegerFieldUpdater<ReadyPicker> indexUpdater =
                AtomicIntegerFieldUpdater.newUpdater(ReadyPicker.class, "index");

        private final List<Subchannel> list; // non-empty

        private final Map<EquivalentAddressGroup, Subchannel> currentSubchannelMap;

        @Nullable
        private final WeightRandomLoadBalancer.StickinessState stickinessState;
        @SuppressWarnings("unused")
        private volatile int index;

        ReadyPicker(List<Subchannel> list,
                    Map<EquivalentAddressGroup, Subchannel> currentSubchannelMap,
                    int startIndex,
                    @Nullable WeightRandomLoadBalancer.StickinessState stickinessState) {
            Preconditions.checkArgument(!list.isEmpty(), "empty list");
            this.list = list;
            this.currentSubchannelMap = currentSubchannelMap;
            this.stickinessState = stickinessState;
            this.index = startIndex - 1;
        }

        @Override
        public PickResult pickSubchannel(PickSubchannelArgs args) {
            Subchannel subchannel = null;
            if (stickinessState != null) {
                String stickinessValue = args.getHeaders().get(stickinessState.key);
                if (stickinessValue != null) {
                    subchannel = stickinessState.getSubchannel(stickinessValue);
                    if (subchannel == null || !WeightRandomLoadBalancer.isReady(subchannel)) {
                        subchannel = stickinessState.maybeRegister(stickinessValue, nextSubchannel());
                    }
                }
            }

            return PickResult.withSubchannel(subchannel != null ? subchannel : nextSubchannel());
        }

        /**
         * 此处实现权重随机负载均衡
         * <p>
         * 思路：
         * 如有4个元素A、B、C、D，权重分别为1、2、3、4，随机结果中A:B:C:D的比例要为1:2:3:4。
         * <p>
         * 总体思路：累加每个元素的权重A(1)-B(3)-C(6)-D(10)，
         * 则4个元素的的权重管辖区间分别为[0,1)、[1,3)、[3,6)、[6,10)。
         * 然后随机出一个[0,10)之间的随机数。落在哪个区间，
         * 则该区间之后的元素即为按权重命中的元素。
         *
         * @return
         */
        private Subchannel nextSubchannel() {
            Map<String, Integer> weightMap = list.get(0).getAttributes().get(GrpcConstants.WEIGHT_LIST_KEY);
            List<WeightSubchannel> weightSubchannelList = Lists.newArrayList();
            Random random = new Random();

            for (Map.Entry<EquivalentAddressGroup, Subchannel> entry : currentSubchannelMap.entrySet()) {
                weightSubchannelList.add(new WeightSubchannel(entry.getValue(),
                        weightMap.get(entry.getKey().getAddresses().get(0).toString().replace("/", ""))));
            }

            Integer weightSum = 0;
            for (WeightSubchannel ws : weightSubchannelList) {
                weightSum += ws.getWeight();
            }
            if (weightSum <= 0) {
                throw new RuntimeException("权重之和应大于0");
            }
            // n in [0, weightSum)
            Integer n = random.nextInt(weightSum);
            Integer m = 0;
            for (WeightSubchannel ws : weightSubchannelList) {
                if (m <= n && n < m + ws.getWeight()) {
                    return ws.getSubchannel();
                }
                m += ws.getWeight();
            }
            //通常不会走到这里，为了保证能得到正确的返回，这里随便返回一个
            return list.get(0);
        }

        @VisibleForTesting
        List<Subchannel> getList() {
            return list;
        }

        @Override
        boolean isEquivalentTo(WeightRandomPicker picker) {
            if (!(picker instanceof ReadyPicker)) {
                return false;
            }
            ReadyPicker other = (ReadyPicker) picker;
            // the lists cannot contain duplicate subchannels
            return other == this || (stickinessState == other.stickinessState
                    && list.size() == other.list.size()
                    && new HashSet<>(list).containsAll(other.list));
        }
    }

    @VisibleForTesting
    static final class EmptyPicker extends WeightRandomPicker {

        private final Status status;

        EmptyPicker(@Nonnull Status status) {
            this.status = Preconditions.checkNotNull(status, "status");
        }

        @Override
        public PickResult pickSubchannel(PickSubchannelArgs args) {
            return status.isOk() ? PickResult.withNoResult() : PickResult.withError(status);
        }

        @Override
        boolean isEquivalentTo(WeightRandomPicker picker) {
            return picker instanceof EmptyPicker && (Objects.equal(status, ((EmptyPicker) picker).status)
                    || (status.isOk() && ((EmptyPicker) picker).status.isOk()));
        }
    }

    /**
     * A lighter weight Reference than AtomicReference.
     */
    @VisibleForTesting
    static final class Ref<T> {
        T value;

        Ref(T value) {
            this.value = value;
        }
    }

    static final class WeightSubchannel {
        private LoadBalancer.Subchannel subchannel;
        private Integer weight;

        public WeightSubchannel(LoadBalancer.Subchannel subchannel, Integer weight) {
            this.subchannel = subchannel;
            this.weight = weight;
        }

        public LoadBalancer.Subchannel getSubchannel() {
            return subchannel;
        }

        public void setSubchannel(LoadBalancer.Subchannel subchannel) {
            this.subchannel = subchannel;
        }

        public Integer getWeight() {
            return weight;
        }

        public void setWeight(Integer weight) {
            this.weight = weight;
        }
    }

}

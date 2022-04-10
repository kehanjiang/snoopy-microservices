package com.snoopy.grpc.base.registry;

import com.snoopy.grpc.base.utils.LoggerBaseUtil;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author :   kehanjiang
 * @date :   2021/11/29  16:03
 */
public class ShutDownHookManager extends Thread {

    private static volatile ShutDownHookManager instance;
    private List<ClosableObject> resourceList;
    /**
     * Priority 值越大，优先级越高
     */
    private static int defaultPriority = 10;

    private ShutDownHookManager() {
        resourceList = new ArrayList<>();
        Runtime.getRuntime().addShutdownHook(this);
    }

    @Override
    public void run() {
        Collections.sort(resourceList);
        try {
            for (ClosableObject closableObject : resourceList) {
                closableObject.closeable.close();
            }
            resourceList.clear();
        } catch (IOException e) {
            LoggerBaseUtil.error(this, e.getMessage(), e);
        }
    }

    public static void registerShutdownHook(Closeable closeable) {
        registerShutdownHook(closeable, defaultPriority);
    }

    public static void registerShutdownHook(Closeable closeable, int priority) {
        if (instance == null) {
            synchronized (ShutDownHookManager.class) {
                if (instance == null) {
                    instance = new ShutDownHookManager();
                }
            }
        }
        instance.appendResourceList(new ClosableObject(closeable, priority));
    }

    private void appendResourceList(ClosableObject closableObject) {
        resourceList.add(closableObject);
    }

    private static class ClosableObject implements Comparable<ClosableObject> {
        Closeable closeable;
        int priority;

        public ClosableObject(Closeable closeable, int priority) {
            this.closeable = closeable;
            this.priority = priority;
        }

        @Override
        public int compareTo(ClosableObject o) {
            if (this.priority > o.priority) {
                return -1;
            } else if (this.priority < o.priority) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}


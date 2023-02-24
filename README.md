微服务架构

支持的注册中心:
https://github.com/kehanjiang/snoopy-microservices-zookeeper-registry.git
https://github.com/kehanjiang/snoopy-microservices-nacos-registry.git
https://github.com/kehanjiang/snoopy-microservices-eureka-registry.git
https://github.com/kehanjiang/snoopy-microservices-etcd-registry.git
https://github.com/kehanjiang/snoopy-microservices-consul-registry.git

gradle 引用方式：

```
dependencies {
    //snoopy微服务
    compile "com.snoopy:snoopy-grpc-spring-boot-starter:${snoopyVersion}"
    //根据需要引入注册中心
    compile "com.snoopy:snoopy-microservices-zookeeper-registry:${snoopyVersion}"
    compile "com.snoopy:snoopy-microservices-consul-registry:${snoopyVersion}"
    compile "com.snoopy:snoopy-microservices-nacos-registry:${snoopyVersion}"
    compile "com.snoopy:snoopy-microservices-etcd-registry:${snoopyVersion}"
    compile "com.snoopy:snoopy-microservices-eureka-registry:${snoopyVersion}"
}
```

`
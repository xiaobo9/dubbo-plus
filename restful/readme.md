# Dubbo Restful 插件文档说明

## 目的

`dubbo` 的扩展版本 `dubbox` 有支持 `restful` ，但是它对 `dubbo` 框架整体改动比较大，导致使用了 `dubbox` 需要把整个 `dubbo` 框架
进行升级替换，个人觉得这个不符合 `dubbo` 的微内核目的，于是就做了一个 `restful` 插件，主要是扩展了 `Protocol` 接口来实现
restful相关内容。

## 将项目支持 Restful

只需要依赖本插件，不需要改动原有代码，简单修改几处配置就可以支持 `http` 请求

### 方法一. 将restful协议整合到你项目当前的servlet容器中

```xml
<dubbo:protocol name='restful' server='servlet' contextpath="dubbo-http/dubbo/api" />
```

针对 `servlet` 容器，需要在你项目的 `web.xml` 中配置 `dubbo` 的 `DispatcherServlet` 的 `servlet`

```xml
<servlet>
    <servlet-name>dubbo</servlet-name>
    <servlet-class>com.alibaba.dubbo.remoting.http.servlet.DispatcherServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>dubbo</servlet-name>
    <url-pattern>/dubbo/api/*</url-pattern>
</servlet-mapping>
```

这样就把 `dubbo` 的接口通过当前 `web` 应用的 `servlet` 暴露出来了。

注意：`contextpath` 是当前 `web` 应用的 【contextpath】 + 【DispatcherServlet的 `url-pattern`】

### 方法二. 通过独立的 jetty 程序提供服务

```xml
<dubbo:protocol name='restful' server='jetty' port="8680" contextpath="dubbo-http/dubbo/api" />
```

## 查看所有发布的服务信息

访问：`http://ip:port/contextpath/services`

## 服务调用

### 服务调用地址：

restful插件是按照:`http://ip:port/contextpath/${path}[/${method}][/${version}][/${group}]`

其中`method`和`version`以及`group`可以不用在路径上体现，可以在请求报文

### 服务调用报文

整个报文都是json格式，格式如下
```
{
method:"请求的方法",
version:"请求的版本",
group:"请求的服务分组",
arg1:"方法第一个参数值，如果是对象，那么就是json对象",
arg2:"方法第二个参数值",
.....
argn:"第n个参数值"
}
```

### 注意

`method` 、 `version` 和 `group` 必须至少在上面报文或者请求路径上出现一次，如果两个地方都出现，那么路径上出现的信息是最终结果

如果默认没有对 `version` 和 `group` 配置，那么对应的值为 `all`

其中 `path` 可以是配置的 `<dubbo:service path='xxx'` path属性，也可以是接口类全名，可以是类名

比如接口 `com.dubboclub.RestfulService` 有方法 `helloWorld` ，该接口的某个服务实现没有配置版本和分组,配置的path属性为 `restfulService`

可以通过下面三种方式访问！

* `http://ip:port/contextpath/com.dubboclub.RestfulService/helloWorld/all/all`
* `http://ip:port/contextpath/restfulService/helloWorld/all/all`
* `http://ip:port/contextpath/RestfulService/helloWorld/all/all`
# Tomcat Performance Modules


## Description

Tomcat standard distribution session manager drop-in replacement, to replace StandardManager.

The Tomcat's standard implementation of its session manager, StandardManager,
stores "active" (hot) sessions in the heap. However, memory is neither
released after session expiry, nor after full garbage collection, resulting
in memory leak in high session throughput and high capacity session object
creation scenarios. This module scans and releases unused session object
references in parallel threads, which fixes the issue, and can perform up to 10-20
fold faster in locating expired session objects. It is capable of releasing
chunks of more than 50k expired sessions, or gigabytes of heap in
sub-milliseconds.

Session objects are long-term objects in the heap, once applications and traffic run at steady state, sessions objects tend to flow into the tenure region of the JVM heap.
However, once sessions expire, unused references shall be freed from the heap as soon as possible for garbage collection, freeing up memory for true application computation use.

## Performance Data

### Test Environment

* AMD Ryzen 9 7900X
* DDR5 5200 64GB

### JVM Environment

* <= JDK 8: -Xms8g -Xmx8g -XX:UseParallelOldGC
* \> JDK 8: -Xms8g -Xmx8g -XX:UseParallelGC

### Test Tools

* Java Mission Control
* JConsole
* JMeter
* Custom benchmark servlets for long-term session objects generation

### Sustained Results

Using the Oracle HotSpot Parallel Collector:
* OldGen / Tenure \> 85% full
* Eden, Survivor \> 50% full

##### ConcurrentStandardSessionManager (Multi-threaded)
* Freeing every ~60k sessions, ~2GB session objects per batch in heap: ~50-100 ms
* Freeing every ~16k sessions, 4-5GB session objects per batch in heap: ~150-300 ms
* Freeing every ~350k sessions, 8GB session objects per batch in heap: ~350-500 ms


##### StandardSessionManager (Single-threaded, JDK 7 Compatible)
* Freeing every ~60k sessions, ~2GB session objects per batch in heap: ~300ms
* Freeing every ~1650k sessions, 4-5GB session objects per batch in heap: ~1300 ms
* Freeing every ~200k-350k sessions, 8GB session objects per batch in heap: ~950-1400 ms

More results and screencaps on different collectors will be available at later times. You could also contact me for contribution of results on different machines and environment.

## Installation, Binaries, Downloads, Artifacts


### Download

The compile binaries (JAR) is available for download here:
* Tomcat 8-9, \>= JDK 8 : https://www.matcphotos.com/cdn_static/dist/jars/msys-tomcat-modules-1.0.0.jar.xz
* Tomcat 7, JDK 7 : https://www.matcphotos.com/cdn_static/dist/jars/msys-tomcat-modules-jdk7-1.0.0.jar.xz

For other combinations of Tomcat versions and JDK, a custom build from source is required.

For a detailed description of JDK and Tomcat compatibility matrix, refer to the Apache Tomcat official documentation at
https://tomcat.apache.org/whichversion.html .

### Install

Decompress and drop in the jar file (msys-tomcat-modules-1.0.0.jar) under the Tomcat's lib directory: CATALINA_BASE/lib
e.g. /opt/tomcat9/lib , C:/tomcat9/lib

### Configuration

Add, or modify the session manager configuration at CATALINA/conf/context.xml, replace, or add the following line:

* Using the multi-threaded version:
`<Manager className="com.mathewsystems.thirdparty.tomcat.catalina.session.ConcurrentStandardSessionManager" />`

* Using the single-threaded version:
`<Manager className="com.mathewsystems.thirdparty.tomcat.catalina.session.StandardSessionManager" />`


* Using the single-threaded version (JDK 7, msys-tomcat-modules-jdk7-1.0.0.jar):
`<Manager className="com.mathewsystems.thirdparty.tomcat.catalina.session.StandardSessionManagerJdk7" />`

For more eager sanitisation of expired sessions, the attribute "processExpiresFrequency" may be used, refer to the Tomcat documentation for details. e.g.:

`
    <Manager className="com.mathewsystems.thirdparty.tomcat.catalina.session.ConcurrentStandardSessionManager" pathname="" processExpiresFrequency="1" />
`

Tomcat configuration reference: https://tomcat.apache.org/tomcat-9.0-doc/config/manager.html


### More Information and Researches

https://www.matcphotos.com/blog

## Tips

* The Tomcat documentation could be too primitive, as in most open-source software, most of the codes have to be traversed to figure out the true logic behind.
When using processExpiresFrequency="1", Tomcat runs the background session clean-up thread at a 10 second interval.

* JVM Heap tuning is application specific. It depends highly on the nature of the application memory usage, and traffic flow. Heap sizing, session object size growth rate,
session durations, active and idle users, session creation rate (new users), incoming traffic rates etc. are all important factors to consider.

## Note

This session manager has no effect on Tomcat servers running in cluster mode, as Tomcat uses DeltaManager as session manager.

## Licensing, Usage and Distribution

The module is distributed under the Apache 2.0 License

See LICENSE for more info.

## Author, Web and Contact

Mathew Chan

https://www.matcphotos.com/blog  

mathew (dot) chan (at) mathewsystems.com


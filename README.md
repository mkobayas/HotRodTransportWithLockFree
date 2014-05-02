HotRodTransportWithLockFree
===========================

Example HotRod Transport using SimpleLockFreeObjectPool


## Performance

HotRod Client Machine

- Intel(R) Xeon(R) CPU E5620 @ 2.40GHz (4core)  * 2 Processor  (8core)
- Red Hat Enterprise Linux Server release 6.4 (Santiago)
- OpenJDK 1.7.0_45

JDG Server Machine

- Intel(R) Core(TM) i7-3770 CPU @ 3.40GHz
- Fedora release 18 (Spherical Cow)
- OpenJDK 1.7.0_19


**Put Operation with 200 Threads**


Original HotRod Client:  
-  62,250 tx/sec  

SimpleLockFreeObjectPool HotRod Client :  
- 107,296 tx/sec


## Point of view

HotRod uses Apache Commons pool. This pooling implementation was coded using **syncronized**, so HotRod Clinet don't use full cpu power under the multi prosessor and multi core evironment.

HotRod with SimpleLockFreeObjectPool'ed transport can use full cpu power under the multi prosessor and multi core evironment.


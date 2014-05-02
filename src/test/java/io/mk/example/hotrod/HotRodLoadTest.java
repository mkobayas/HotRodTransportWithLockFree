package io.mk.example.hotrod;


import java.util.Properties;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class HotRodLoadTest {

	@Option(name="-h", usage="help")
	public static boolean help;
	
	@Option(name="-m", metaVar="cacheName", usage="cacheName")
	public static String cacheName = "c1";
	
	@Option(name="-n", metaVar="count", usage="count per thread")
	public static int count = 10000;

	@Option(name="-t", metaVar="th_num", usage="threads count")
	public static int th_num = 15;

	@Option(name="-l", metaVar="loop", usage="Loop count")
	public static int loop = 1;

	@Option(name="-p", usage="PutIfAbsent Operation otherwise Get Operation")
	public static boolean put;

	@Option(name="-c", usage="clearOnStartup")
	public static boolean clearOnStartup;
	
	@Option(name="-s", metaVar="serverList",usage="List of Servers")
	public static String serverList = "10.64.193.144:11222";
	
	@Option(name="-f", usage="use lockfree")
	public static boolean useLockFree;
	
	
	// Internal use.
	private static RemoteCacheManager manager;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) throws Exception {
		
		// parse argument
		HotRodLoadTest app = new HotRodLoadTest();
        CmdLineParser parser = new CmdLineParser(app);
        try {
            parser.parseArgument(args);    
        } catch (CmdLineException e) {
            System.err.println(e);
            parser.printUsage(System.err);
            System.exit(1);
        }
		
        if(help) {
            parser.printUsage(System.err);
            System.exit(1);
        }
		
        
        Properties props = new Properties();
        props.put(ConfigurationProperties.SERVER_LIST, serverList);
        
        if(useLockFree) {
        	props.put(ConfigurationProperties.TRANSPORT_FACTORY, LockFreePooledTcpTransportFactory.class.getName());
        }
        
        manager = new RemoteCacheManager( props);
        
        Runtime.getRuntime().addShutdownHook(new JdgStopHook(manager));
		System.out.println("Shutdown Hot Regist");
        
        RemoteCache cache = manager.getCache(cacheName);
        
        if(clearOnStartup) {
        	cache.clear();
    		System.out.println("Clear");
		}
		
		System.out.println("START");
		long startT = System.currentTimeMillis();
		
		Thread[] t = new Thread[th_num];
		for(int i=0; i<t.length ; i++) {
			
			t[i] = new Thread(new LoadThread(cache, i+1, count, loop, put));
			t[i].start();
		}
		
		for(int i=0; i<t.length ; i++) {
			t[i].join();
		}
		long end = System.currentTimeMillis();
		System.out.println((end -startT) + " ms " + (int)(count*th_num*loop/((end-startT)/1000d)) + "tx/sec");
		
    }
	
	static class LoadThread implements Runnable {

		final String PAD = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
		
		private int threadNum;
		private int count;
		private int loop;
		private RemoteCache<Object, Object> cache;
		boolean put;
		
		public LoadThread(RemoteCache<Object, Object> cache, int threadNum, int count, int loop, boolean put) {
			this.cache = cache;
			this.threadNum = threadNum;
			this.count = count;
			this.loop = loop;
			this.put = put;
		}
		
		@Override
		public void run() {
			
			int current = 0;
			
			for(int j=1 ; j<=loop; j++) {
				

				int ng = 0;
				int ok = 0;
				
				long startT = System.currentTimeMillis();
				
				for(int i=j*count ; i< (j+1)*count ; ) {
					String key = ("A|" + threadNum + "|" + current + "|" + PAD).substring(0, 100);
					String value = "";
					Object ret = null;
					
					if(put) {
						try {
							ret = cache.put(key , value);
							i++;
							current++;
						} catch(Exception e) {
							try {
								e.printStackTrace();
								System.out.println("エラー発生 リトライします" + key);
								Thread.sleep(500);
								continue;
							} catch (InterruptedException e1) {
							}
						}
						
						if( ret != null) {
							ng++;
						} else {
							ok++;
						}
					} else {
						try {
							ret = cache.get(key);
							i++;
							current++;
						} catch(Exception e) {
							try {
								e.printStackTrace();
								System.out.println("エラー発生 リトライします" + key);
								Thread.sleep(500);
								continue;
							} catch (InterruptedException e1) {
							}
						}
						
						if( ret == null) {
							ng++;
						} else {
							ok++;
						}
						
					}
				}
				long end = System.currentTimeMillis();
				
				System.out.println(threadNum + "TH OK=" + ok + " NG=" + ng + ": " + (end - startT)  + "ms " + (int)(count/((end-startT)/1000d)) + "tx/sec ");
				
			}
			
		}
		
	}
	
	static class JdgStopHook extends Thread {
		RemoteCacheManager manager;
		
		public JdgStopHook(RemoteCacheManager manager) {
			this.manager = manager;
		}
		@Override
		public void run() {
			manager.stop();
		}
		
	}
	
}






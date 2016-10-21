# thone
<h3>A tool to make a process having only one instance in a cluster</h3>

<hr>

## Requirement

* Zookeeper
* Java 8
 
## Installation
<pre><code>
&lt;dependency&gt;
  &lt;groupId&gt;com.jiuxian&lt;/groupId&gt;
  &lt;artifactId&gt;theone&lt;/artifactId&gt;
  &lt;version&gt;1.2.1-RELEASE&lt;/version&gt;
&lt;/dependency&gt;
</code></pre>

## Key concept

* Process
 * 进程
* UniqueProcess
 * 用于包装进程，保证进程在集群中只有惟一一份实例在运行


## Quick Start

#### Implement a simple process
<pre><code>
public class SimpleProcess implements Process {
    
	@Override
	public void run() {
		System.out.println("This is a process example.");
	}

	@Override
	public void close() throws Exception {
		System.out.println("Close resources");
	}

}
</code></pre>

#### Wrap the process with UniqueProcess
<pre><code>
	String zks = "192.168.5.99,192.168.5.104";
	Process process = new SimpleProcess();
	UniqueProcess guard = new ZookeeperUniqueProcess(process, zks);
	guard.run();
</code></pre>

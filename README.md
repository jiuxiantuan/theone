# theone
<h3>A tool to make a process having only one instance in a cluster</h3>

<hr>

## Requirement

* Zookeeper
* Java 8
 
## Installation
```
<dependency>
  <groupId>com.jiuxian</groupId>
  <artifactId>theone</artifactId>
  <version>1.4.1-RELEASE</version>
</dependency>
```

## Key concept

* Process
 * 进程
* Competitive
 * 进程的可竞争性实现，用于保证进程在集群中只有惟一一份实例在运行


## Quick Start

#### Implement a simple process
```
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
```

#### Wrap the process with UniqueProcess
```
	String zks = "localhost";
	Process process = new SimpleProcess();
	Competitive competitive = new ZookeeperCompetitiveImpl(zks, "group1");
	CompetitiveProcess guard = new CompetitiveProcess(process, competitive);
	guard.run();
```

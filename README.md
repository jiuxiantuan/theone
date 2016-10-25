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
  <version>1.3.0-RELEASE</version>
</dependency>
```

## Key concept

* Process
 * 进程
* UniqueProcess
 * 用于包装进程，保证进程在集群中只有惟一一份实例在运行


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
		try (CompetitiveProcess guard = new ZookeeperCompetitiveProcess(process, zks, "group1")) {
			guard.run();
		} catch (Exception e) {
		}
```

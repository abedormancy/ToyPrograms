package tool.java;

/**
 * 分布式 id 生成, 代码来自于 Twitter SnowFlake 算法
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * 可使用69年，( (1 << 41) / (3600 * 24 * 365 * 1000) ~= 69 )
 * 1 bit 符号位，正数id，所以为 0
 * 41 bit timestamp (存储的是timestamp的差值)
 * 10 bit 节点位，5位数据中心，5位机器
 * 12 bit sequence (每毫秒最多可生成 4,096个id)
 */
public class SnowFlake {

    private final static long START_STAMP = 636600000000L;

    private final static long DATACENTER_BIT = 5;
    private final static long MACHINE_BIT = 5;
    private final static long SEQUENCE_BIT = 12;

    // 掩码
    private final static long DATACENTER_MASK = -1L ^ (-1L << DATACENTER_BIT);
    private final static long MACHINE_MASK = -1L ^ (-1L << MACHINE_BIT);
    private final static long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BIT);

    // 左移位
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private long datacenterId;
    private long machineId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowFlake(long datacenterId, long machineId) {
        if (datacenterId > DATACENTER_MASK || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than DATACENTER_MASK or less than 0");
        }
        if (machineId > MACHINE_MASK || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MACHINE_MASK or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d seconds",
                    lastTimestamp - timestamp));
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0L) {
                timestamp = nextTime();
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return (timestamp - START_STAMP) << TIMESTMP_LEFT
                | datacenterId << DATACENTER_LEFT
                | machineId << MACHINE_LEFT
                | sequence;
    }

    private long nextTime() {
        long timestamp = -1;
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    public static void main(String[] args) {
        SnowFlake idGenerator = new SnowFlake(0, 0);
        for (int i = 0; i < 20; i++) {
            System.out.println(idGenerator.nextId());
        }
    }
}

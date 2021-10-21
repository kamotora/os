package processor;

import java.util.Collection;
import java.util.LongSummaryStatistics;

public record ProcessorStatistics(LongSummaryStatistics execTimeStat, LongSummaryStatistics fromExecStat,
                                  LongSummaryStatistics fromStartStat, LongSummaryStatistics waitTimeStat,
                                  long dispatcherTime, long totalTime, long calcOperationsTime,
                                  long ioOperationsTime) {

    public static ProcessorStatistics sum(Collection<ProcessorStatistics> stats) {
        return stats
                .stream()
                .reduce(createZero(), ProcessorStatistics::sum);
    }

    /**
     * immutable sum
     *
     * @param a first stat
     * @param b second stat
     * @return new summed stat
     */
    public static ProcessorStatistics sum(ProcessorStatistics a, ProcessorStatistics b) {
        return new ProcessorStatistics(
                sumStats(a.execTimeStat, b.execTimeStat),
                sumStats(a.fromExecStat, b.fromExecStat),
                sumStats(a.waitTimeStat, b.waitTimeStat),
                sumStats(a.fromStartStat, b.fromStartStat),
                Long.sum(a.dispatcherTime, b.dispatcherTime),
                Long.sum(a.totalTime, b.totalTime),

                Long.sum(a.calcOperationsTime, b.calcOperationsTime),
                Long.sum(a.ioOperationsTime, b.ioOperationsTime)
        );
    }

    private static LongSummaryStatistics sumStats(LongSummaryStatistics a, LongSummaryStatistics b) {
        var res = new LongSummaryStatistics(a.getCount(), a.getMin(), a.getMax(), a.getSum());
        res.combine(b);
        return res;
    }

    public static ProcessorStatistics createZero() {
        return new ProcessorStatistics(createZeroStat(), createZeroStat(), createZeroStat(), createZeroStat(),
                0L, 0L, 0L, 0L);
    }

    private static LongSummaryStatistics createZeroStat() {
        return new LongSummaryStatistics(0, 0, 0, 0);
    }
}

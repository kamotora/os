package processor;

import java.util.LongSummaryStatistics;

public record ProcessorStatistics(LongSummaryStatistics execTimeStat, LongSummaryStatistics fromExecStat,
                                  LongSummaryStatistics fromStartStat, LongSummaryStatistics waitTimeStat,
                                  long dispatcherTime, long totalTime, long calcOperationsTime, long ioOperationsTime) {
}

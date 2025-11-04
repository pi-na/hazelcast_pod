package ar.edu.itba.pod.api.longestWait;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

//                                   LineaCSV                      pickUpLocationID
public class LongestWaitMapper implements Mapper<Long, LongestWaitMapperValueIn, Long, LongestWaitReducerValue> {
    @Override
    public void map(Long keyIn, LongestWaitMapperValueIn longestWaitMapperValueIn, Context<Long, LongestWaitReducerValue> context) {
        context.emit(longestWaitMapperValueIn.puLocationId(),
                new LongestWaitReducerValue(longestWaitMapperValueIn.doLocationId(), longestWaitMapperValueIn.waitMillis(), longestWaitMapperValueIn.zoneName()));
    }
}

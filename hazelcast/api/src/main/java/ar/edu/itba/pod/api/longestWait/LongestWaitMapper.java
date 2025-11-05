package ar.edu.itba.pod.api.longestWait;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

//                                              LineaCSV                      pickUpLocationID
public class LongestWaitMapper implements Mapper<Long, LongestWaitMapperValueIn, Integer, LongestWaitReducerValue> {
    @Override
    public void map(Long keyIn, LongestWaitMapperValueIn longestWaitMapperValueIn, Context<Integer, LongestWaitReducerValue> context) {
        context.emit(longestWaitMapperValueIn.getPULocation(),
                new LongestWaitReducerValue(longestWaitMapperValueIn.getDOLocation(),
                        longestWaitMapperValueIn.waitMillis(longestWaitMapperValueIn.getRequest_datetime(), longestWaitMapperValueIn.getPickup_datetime()),
                        longestWaitMapperValueIn.getDropoffZone(),
                        longestWaitMapperValueIn.getPickUpZone()));
    }
}

package com.community.service.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdminStatsMapper {

    @Select("""
        select
          (select count(*) from users) as totalUsers,
          (select count(*) from volunteer_profiles where audit_status = 'APPROVED') as approvedVolunteers,
          (select count(*) from demands where status = 'PENDING') as pendingDemands,
          (select ifnull(sum(service_hours), 0) from registrations where status = 'CHECKED_OUT') as totalServiceHours
        """)
    Map<String, Object> totals();

    @Select("""
        select
          date(r.check_out_time) as day,
          ifnull(sum(r.service_hours), 0) as serviceHours,
          count(*) as finishedCount
        from registrations r
        where r.status = 'CHECKED_OUT'
          and r.check_out_time >= date_sub(curdate(), interval 6 day)
        group by date(r.check_out_time)
        order by day
        """)
    List<Map<String, Object>> last7DaysService();

    @Select("""
        select
          date(p.created_at) as day,
          ifnull(sum(case when p.amount > 0 then p.amount else 0 end), 0) as pointsIn,
          ifnull(sum(case when p.amount < 0 then -p.amount else 0 end), 0) as pointsOut
        from point_records p
        where p.created_at >= date_sub(curdate(), interval 6 day)
        group by date(p.created_at)
        order by day
        """)
    List<Map<String, Object>> last7DaysPoints();
}


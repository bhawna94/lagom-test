package edu.knoldus.impl.emp;

import akka.NotUsed;
import com.google.inject.Inject;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import edu.knoldus.api.emp.Employee;
import edu.knoldus.api.emp.EmployeeService;
import edu.knoldus.impl.emp.Repository.Operation;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class EmployeeServiceImpl implements EmployeeService {

    private CassandraSession cassandraSession;

    @Inject
    public EmployeeServiceImpl(CassandraSession cassandraSession) {
        this.cassandraSession = cassandraSession;
    }


    @Override
    public ServiceCall<NotUsed, Employee> getEmployeeDetail(int employeeId) {
        return request -> cassandraSession.selectOne(Operation.GETUSER, employeeId)
                .thenApply(row -> row.map(user -> Employee.builder()
                        .employeeId(user.getInt("e_id"))
                        .employeeName(user.getString("name"))
                        .age(user.getInt("age"))
                        .gender(user.getString("gender"))
                        .lastPaid(user.getString("last_paid"))
                        .totalDues(user.getInt("total_dues"))
                        .build())
                        .orElseGet(() -> {
                            throw new NotFound("Employee with eid " + employeeId + " does not exist");
                        }));
    }

    @Override
    public ServiceCall<Employee, String> postEmployeeDetail() {
        return request -> {
            CompletionStage<List<Integer>> employeeIds = cassandraSession.selectAll(Operation.ALLUSER)
                    .thenApply(row -> row.stream().map(y -> y.getInt("e_id")).collect(Collectors.toList()));

            employeeIds.thenCompose(row -> {
                System.out.println(row);
                boolean value = row.stream().anyMatch(x -> x == request.getEmployeeId());
                if (!value) {
                    cassandraSession.executeWrite(Operation.ADDUSER, request.getEmployeeId(),
                            request.getAge(),
                            request.getGender(),
                            request.getLastPaid(),
                            request.getEmployeeName(),
                            request.getTotalDues());
                    return CompletableFuture.completedFuture("inserted");
                }
                else
                    return CompletableFuture.completedFuture("user already exist");
            });
            return CompletableFuture.completedFuture(".....");

        };
    }

    @Override
    public ServiceCall<NotUsed, String> deleteEmployeeDetail(int employeeId) {
        return request -> {
            CompletionStage<List<Integer>> employeeIds = cassandraSession.selectAll(Operation.ALLUSER)
                    .thenApply(row -> row.stream().map(y -> y.getInt("e_id")).collect(Collectors.toList()));

            employeeIds.thenCompose(row -> deleteUser(row,employeeId) );
            return CompletableFuture.completedFuture("......");
        };
    }

    public CompletableFuture<String> deleteUser(List<Integer> list, int employeeId){

        boolean value = list.stream().anyMatch(x -> x == employeeId);
        if(value){
            cassandraSession.executeWrite(Operation.DELETEUSER, employeeId);
            return CompletableFuture.completedFuture("user deleted");
        }
        else
            return CompletableFuture.completedFuture("user does not exist");
    }


}

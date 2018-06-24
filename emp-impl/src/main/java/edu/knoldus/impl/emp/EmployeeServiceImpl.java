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
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class EmployeeServiceImpl implements EmployeeService {

    private CassandraSession cassandraSession;

    @Inject
    public EmployeeServiceImpl(CassandraSession cassandraSession) {
        this.cassandraSession = cassandraSession;
    }

    /**
     * get employee details using get()
     *
     * @param employeeId
     * @return
     */
    @Override
    public ServiceCall<NotUsed, Employee> getEmployeeDetail(int employeeId) {
        return request -> cassandraSession.selectOne(Operation.GETUSER, employeeId)
                .thenApply(row -> row.map(employee -> Employee.builder()
                        .employeeId(employee.getInt("e_id"))
                        .employeeName(employee.getString("name"))
                        .age(employee.getInt("age"))
                        .gender(employee.getString("gender"))
                        .lastPaid(employee.getString("last_paid"))
                        .totalDues(employee.getFloat("total_dues")).build()).get())
                .exceptionally(throwable -> {
                    Throwable cause = throwable.getCause();
                    if (cause instanceof NoSuchElementException)
                        throw new NotFound("employee with employeeId " + employeeId + " does not exist");
                    throw new RuntimeException(cause);
                });

    }

    /**
     * add employee using join()
     *
     * @return
     */
    @Override
    public ServiceCall<Employee, String> postEmployeeDetail() {
        return request -> {
            List<Integer> op = cassandraSession.selectAll(Operation.ALLUSER)
                    .thenApply(row -> row.stream().map(x -> x.getInt("e_id"))
                            .collect(Collectors.toList()))
                    .toCompletableFuture()
                    .join();
            System.out.println("....." + op);
            boolean value = op.stream().anyMatch(x -> x == request.getEmployeeId());

            if (!value) {
                cassandraSession.executeWrite(Operation.ADDUSER,
                        request.getEmployeeId(),
                        request.getAge(),
                        request.getGender(),
                        request.getLastPaid(),
                        request.getEmployeeName(),
                        request.getTotalDues());
                return CompletableFuture.completedFuture("inserted");

            } else return CompletableFuture.completedFuture("user already exist");
        };
    }

    /**
     * delete employee using join()
     *
     * @param employeeId
     * @return
     */
    @Override
    public ServiceCall<NotUsed, String> deleteEmployeeDetail(int employeeId) {
        return request -> cassandraSession.executeWrite(Operation.DELETEUSER, employeeId)
                .thenApply(done -> "User deleted")
                .exceptionally(throwable -> {
                    Throwable cause = throwable.getCause();
                    if (cause instanceof NoSuchElementException)
                        throw new NotFound("employee with employeeId " + employeeId + " does not exist");
                    throw new RuntimeException(cause);
                });
    }


}

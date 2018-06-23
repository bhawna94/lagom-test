package edu.knoldus.impl.emp;

import akka.NotUsed;
import com.google.inject.Inject;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import edu.knoldus.api.emp.Employee;
import edu.knoldus.api.emp.EmployeeService;
import edu.knoldus.impl.emp.Repository.Operation;

import java.util.NoSuchElementException;

public class EmployeeServiceImpl implements EmployeeService {

    private CassandraSession cassandraSession;

    @Inject
    public EmployeeServiceImpl(CassandraSession cassandraSession) {
        this.cassandraSession = cassandraSession;
    }


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


}

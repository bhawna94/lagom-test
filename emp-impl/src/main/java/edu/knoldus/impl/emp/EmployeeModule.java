package edu.knoldus.impl.emp;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import edu.knoldus.api.emp.EmployeeService;

public class EmployeeModule extends AbstractModule implements ServiceGuiceSupport {


    @Override
    protected void configure()
    {
        bindService(EmployeeService.class, EmployeeServiceImpl.class);
    }
}

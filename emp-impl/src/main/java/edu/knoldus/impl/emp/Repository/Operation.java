package edu.knoldus.impl.emp.Repository;

public class Operation {

   public static final String GETUSER = "Select * from lagom_test.employee where e_id = ?";
   public static final String ALLUSER = "Select e_id from lagom_test.employee";
   public static final String ADDUSER = "insert into lagom_test.employee (e_id,age,gender,last_paid,name,total_dues) values(?,?,?,?,?,?)";
   public static final String DELETEUSER = "delete from lagom_test.employee where e_id = ?";
   public static final String UPDATEUSER = "update lagom_test.employee set total_dues = ? where e_id = ?";
}

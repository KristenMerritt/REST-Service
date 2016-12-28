import Service.*;
import Business.*;

public class TestMyCode {

   public static void main(String[] args){
      LAMSService service = new LAMSService();
      //BusinessLayer bus = new BusinessLayer();
      System.out.println("-------------------------------");
      System.out.println("~~ GETTING ALL APPOINTMENTS ~~");
      System.out.println("-------------------------------");
      System.out.println(service.getAllAppointments());
      
      System.out.println("-------------------------------");
      System.out.println("~~ ADDING APPOINTMENTS ~~");
      System.out.println("-------------------------------");
      System.out.println(service.addAppointment("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?><appointment><date>2016-12-30</date><time>10:00</time><patientId>220</patientId><physicianId>20</physicianId><pscId>520</pscId><phlebotomistId>110</phlebotomistId><labTests><test id=\"86900\" dxcode=\"292.9\" /><test id=\"86609\" dxcode=\"307.3\" /></labTests></appointment>"));         
      System.out.println(service.addAppointment("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?><appointment><date>2016-12-30</date> <time>10:05</time><patientId>220</patientId><physicianId>20</physicianId><pscId>520</pscId><phlebotomistId>110</phlebotomistId><labTests><test id=\"86900\" dxcode=\"292.9\" /><test id=\"86609\" dxcode=\"307.3\" /></labTests></appointment>")); 
      
      
      System.out.println("-------------------------------");
      System.out.println("~~ GETTING APPOINTMENT 791 ~~");
      System.out.println("-------------------------------");
      System.out.println(service.getAppointment("791"));
   }


}
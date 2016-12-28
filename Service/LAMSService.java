package Service;

import javax.jws.*;
import java.util.*;
import components.data.*;
import Business.*;
import java.sql.Time;
import java.text.SimpleDateFormat;
import javax.ws.rs.core.*;
import javax.ws.rs.*;


@Path("LAMSService")
public class LAMSService{
   private String	appointmentString;
   private static BusinessLayer bus = new BusinessLayer();
   
	@Context
   private UriInfo context;

//    public LAMSService(){
//       bus =	new BusinessLayer();
//       System.out.println(initialize());
//    }

   @GET
   @Produces("application/xml")
   public String initialize(){
      bus.initialize();
      return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><AppointmentList><intro>Welcome to the LAMS Appointment Service</intro><wadl>"+this.context.getBaseUri().toString()+"application.wadl</wadl></AppointmentList>";
   }
   


    @Path("/Appointments")
    @GET
    @Produces("application/xml")
   public String getAllAppointments(){
      List<Object> objs = bus.getAllAppointments();
      String url = this.context.getBaseUri().toString();
      appointmentString = bus.transformObjectsToXML(objs, url);
      return appointmentString;
   }
	
   
   
    @Path("/Appointments/{appointNumber}")
    @GET
    @Consumes({"text/xml","application/xml"})
    @Produces("application/xml")
    public String getAppointment(@PathParam("appointNumber") String appointNumber){
      List<Object> objs	= bus.getAppointment(appointNumber);
   	
      if(objs.size()	<=	0){
         return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><AppointmentList><error>Appointment does not exist</error></AppointmentList>";
      } else {
         String url = this.context.getBaseUri().toString();
         appointmentString = bus.transformObjectsToXML(objs, url);
      }
      return appointmentString;
   
   }
	
   
   
    @Path("/Appointments")
    @PUT
    @Consumes({"text/xml","application/xml"})
    @Produces("application/xml")
   public String addAppointment(String	xml){
      ArrayList<String>	validation = bus.validateAppointment(xml);
      String response =	validation.get(0);
      String dateString	= validation.get(1);
      String timeString	= validation.get(2);
      String phlebID	= validation.get(3);
      String pscID =	validation.get(4);
      String patientID = validation.get(5);
      String appointmentID = bus.getNextAppointmentID();
   	
      String xmlResponse =	null;
   	
      if(response.equals("Appointment can be scheduled.")){
         java.sql.Date date =	java.sql.Date.valueOf(dateString.toString());
         Time time =	java.sql.Time.valueOf(timeString);
      	
         Appointment	newAppt = new Appointment();
         newAppt.setApptdate(date);
         newAppt.setAppttime(time);
         newAppt.setId(appointmentID);
         
         Patient patient =	bus.getPatient(patientID);
         Phlebotomist phleb =	bus.getPhleb(phlebID);
         PSC psc = bus.getPsc(pscID);
      	
         newAppt.setPatientid(patient);
         newAppt.setPhlebid(phleb);
         newAppt.setPscid(psc);
      
      	//extra steps here due to persistence api	and join, need	to	create objects	in	list
         List<AppointmentLabTest> tests =	bus.getLabTests(xml, appointmentID);
         //System.out.println("Lab tests: " + tests.size());
      	newAppt.setAppointmentLabTestCollection(tests);
         List<AppointmentLabTest> testsCount = newAppt.getAppointmentLabTestCollection();
         //System.out.println("Lab tests 2: " + testsCount.size());
      	boolean good = bus.insertAppointment(newAppt); 
         
         if(good){
            xmlResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><AppointmentList><uri>"+this.context.getBaseUri().toString()+ "LAMSService/Appointments/" + appointmentID + "</uri></AppointmentList>";
         } else {
            xmlResponse	= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><AppointmentList><error>Error creating appointment</error></AppointmentList>";
         }
      } else {
         xmlResponse	= "<?xml version=\"1.0\" encoding=\"UTF-8\"?><AppointmentList><error>" + response +	"</error></AppointmentList>";
      }
      return xmlResponse;
   }
}
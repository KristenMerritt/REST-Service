package Business;

import Service.*;
import java.util.*;
import components.data.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import java.io.StringReader;
import javax.xml.parsers.*;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class BusinessLayer{
   private String appointmentString;
   private NodeList allAppointmentsList;
   
   
//    /**
//     * Default constructor 
//     */
//    public BusinessLayer(){
//      //initialize();
//    }
   
   
   /**
    * Initializes database.
    * @return String
    */
   public void initialize() {
        DBSingleton.getInstance().db.initialLoad("LAMS");
    }
   

   
   /**
    * Retrieves all appointments from database
    * @return List<Object> objs
    */
   public List<Object> getAllAppointments(){
      List<Object> objs = DBSingleton.getInstance().db.getData("Appointment", "");
      return objs;
   }
   
   
   /**
    * Returns a list that only contains one Appointment object based on appointmentNumer
    * @param String appointmentNumber
    * @return List<Object> objs
    */
    public List<Object> getAppointment(String appointNumber){
        List<Object> objs = DBSingleton.getInstance().db.getData("Appointment", "ID = '" + appointNumber + "'");
        return objs;    
    }
   
   
    /**
    * Retrieves all appointments from database
    * @return List<Object> objs
    */   
    public String getAppointment(String table, String column, String data){
        List<Object> objs = DBSingleton.getInstance().db.getData(table, column+" = '" + data + "'");
        
        if(objs.size() <= 0){
            appointmentString = "";
        } else {
            appointmentString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><AppointmentList>";
            
            for (Object obj : objs){
              Appointment appoint = (Appointment)obj;
              Patient patient = appoint.getPatientid();
              Phlebotomist phleb = appoint.getPhlebid();
              PSC psc = appoint.getPscid();
              List<AppointmentLabTest> labTests = appoint.getAppointmentLabTestCollection();
                     
              appointmentString += "<appointment date=\"" + appoint.getApptdate() + "\" id=\"" + appoint.getId() + "\" time=\"" + appoint.getAppttime() + "\">";
              appointmentString += "<patient id=\"" + patient.getId() + "\"><uri/><name>" + patient.getName() + "</name><address>" + patient.getAddress() + "</address><insurance>" + patient.getInsurance() + "</insurance><dob>" + patient.getDateofbirth() + "</dob></patient>";
              appointmentString += "<phlebotomist id=\"" + phleb.getId() + "\"><uri/><name>" + phleb.getName() + "</name></phlebotomist>";
              appointmentString += "<psc id=\"" + psc.getId() + "\"><uri/><name>" + psc.getName() + "</name></psc>";
              appointmentString += "<allLabTests>";
                  
              for(AppointmentLabTest test : labTests){
                 LabTest lt = test.getLabTest();
                 Diagnosis dia = test.getDiagnosis();
                 appointmentString += "<appointmentLabTest appointmentId=\"" + appoint.getId() + "\" dxcode=\"" +  dia.getCode() + "\" labTestId=\"" + lt.getId() + "\"><uri/></appointmentLabTest>";
              }
                  
              appointmentString += "</allLabTests>";
              appointmentString += "</appointment>";
            }
            appointmentString += "</AppointmentList>";
           }
           return appointmentString;
   }
   
   
   private ArrayList<ArrayList<String>> getNewAppointsInfo(String xml){
      xml = xml.trim().replaceAll("[^\\x20-\\x7e\\x0A]", "");
      ArrayList<ArrayList<String>> appointmentsInfo = null;
      NamedNodeMap attributes = null;
      
      try {
         appointmentsInfo = new ArrayList<ArrayList<String>>();
         
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
         Document parsedXML = db.parse(stream);
          
         NodeList appointmentsList = parsedXML.getElementsByTagName("appointment");
         
         for(int i=0; i < appointmentsList.getLength(); i++){
            ArrayList<String> newAppointmentInfo = new ArrayList<String>();
            
            Node appointment = appointmentsList.item(i);
                                    
            // date, time, psc, phleb, patient
            NodeList children = appointment.getChildNodes(); 
            
            int childrenCount = children.getLength();
            Node phleb = null, psc = null, patient = null, date = null, time = null;
            for (int j = 0; j < childrenCount; j++){
            	Node theNode = children.item(j);
            	if ("phlebotomistId".equals(theNode.getNodeName())){
            		phleb = theNode;
            	}
            	if ("pscId".equals(theNode.getNodeName())){
            		psc = theNode;
            	}
               if ("patientId".equals(theNode.getNodeName())){
            		patient = theNode;
            	}
               if ("date".equals(theNode.getNodeName())){
            		date = theNode;
            	}
               if ("time".equals(theNode.getNodeName())){
            		time = theNode;
            	}
            }
            
            String dateString = date.getTextContent();
            newAppointmentInfo.add(dateString);
            
            String timeString = time.getTextContent();
            newAppointmentInfo.add(timeString);
            
            String phlebId = phleb.getTextContent();
            newAppointmentInfo.add(phlebId);
   
            String pscId = psc.getTextContent();
            newAppointmentInfo.add(pscId);
            
            String patientId = patient.getTextContent();
            newAppointmentInfo.add(patientId);

            appointmentsInfo.add(newAppointmentInfo);
         }
         
         return appointmentsInfo;
      }
      catch(ParserConfigurationException pce){
         pce.printStackTrace();
         return appointmentsInfo;
      }
      catch(SAXException sax){
         sax.printStackTrace();
         return appointmentsInfo;
      }
      catch(Exception e){
         e.printStackTrace();
         return appointmentsInfo;
      }   
   }


   /**
    * Parses through appointments xml and returns information in nested arraylist
    * @param String xml
    * @return ArrayList<ArrayList<String>> appointmentsInfo
    */
   private ArrayList<ArrayList<String>> getAppointsInfo(String xml){
      xml = xml.trim().replaceAll("[^\\x20-\\x7e\\x0A]", "");
      ArrayList<ArrayList<String>> appointmentsInfo = null;
      NamedNodeMap attributes = null;
      
      try {
         appointmentsInfo = new ArrayList<ArrayList<String>>();
         
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
         Document parsedXML = db.parse(stream);
          
         NodeList appointmentsList = parsedXML.getElementsByTagName("appointment");
         
         for(int i=0; i < appointmentsList.getLength(); i++){
            ArrayList<String> newAppointmentInfo = new ArrayList<String>();
            
            Node appointment = appointmentsList.item(i);
            attributes = appointment.getAttributes();   
            String date = attributes.getNamedItem("date").getNodeValue();
            String time = attributes.getNamedItem("time").getNodeValue();
            String id = attributes.getNamedItem("id").getNodeValue();
            newAppointmentInfo.add(date);
            newAppointmentInfo.add(time);
            newAppointmentInfo.add(id);
                        
            // patient, phleb, psc elements of current appointment
            NodeList children = appointment.getChildNodes(); 
            
            int childrenCount = children.getLength();
            Node phleb = null, psc = null, patient = null, tests = null;
            for (int j = 0; j < childrenCount; j++){
            	Node theNode = children.item(j);
            	if ("phlebotomist".equals(theNode.getNodeName())){
            		phleb = theNode;
            	}
            	if ("psc".equals(theNode.getNodeName())){
            		psc = theNode;
            	}
               if ("patient".equals(theNode.getNodeName())){
            		patient = theNode;
            	}
              
            }
            
            attributes = phleb.getAttributes(); 
            String phlebId = attributes.getNamedItem("id").getNodeValue();
            newAppointmentInfo.add(phlebId);
   
            attributes = psc.getAttributes(); 
            String pscId = attributes.getNamedItem("id").getNodeValue();
            newAppointmentInfo.add(pscId);
            
            attributes = patient.getAttributes(); 
            String patientId = attributes.getNamedItem("id").getNodeValue();
            newAppointmentInfo.add(patientId);
            
            appointmentsInfo.add(newAppointmentInfo);
         }
         
         return appointmentsInfo;
      }
      catch(ParserConfigurationException pce){
         pce.printStackTrace();
         return appointmentsInfo;
      }
      catch(SAXException sax){
         sax.printStackTrace();
         return appointmentsInfo;
      }
      catch(Exception e){
         e.printStackTrace();
         return appointmentsInfo;
      }   
   }
   

   /**
    * Validates whether or not a time is between a start and end time. Returns true if it is between them, false if it is not
    * @param String checkString
    * @param String startString
    * @param String endString
    * @return boolean valid
    */
   private boolean validateTime(String checkString, String startString, String endString){
      boolean valid;
      try{
            valid = false;
            //Check Time
            java.util.Date checkTime = new SimpleDateFormat("HH:mm:ss").parse(checkString);
            Calendar checkCalendar = Calendar.getInstance();
            checkCalendar.setTime(checkTime);

            //Start Time
            java.util.Date startTime = new SimpleDateFormat("HH:mm:ss").parse(startString);
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(startTime);

            //End Time
            java.util.Date endTime = new SimpleDateFormat("HH:mm:ss").parse(endString);
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(endTime);

            java.util.Date checkingTime = checkCalendar.getTime();
            if ((checkingTime.compareTo(startCalendar.getTime()) == 0) || checkingTime.after(startCalendar.getTime()) && checkingTime.before(endCalendar.getTime())) {
                valid = true;
            }
         }
         catch(ParseException p){
            System.out.println(p.getMessage());
            valid = false;
         }
         catch(Exception e){
            System.out.println(e.getMessage());
            valid = false;
         }
         return valid;
    }
   

   /**
    * Returns the new appointment info. First item in the arraylist is the response. If it can be scheduled, response = 'Appointment can be scheduled.'. If it cannot, response = the error string.
    * @param String xml
    * @return ArrayList<String> appointmentInfo
    */
   public ArrayList<String> validateAppointment(String xml){
         String response = "";
         ArrayList<ArrayList<String>> phlebAppointmentsThatDay = new ArrayList<ArrayList<String>>(); // appointments for the phleb on the requested day

         // NEW APPOINTMENT INFORMATION 
         ArrayList<ArrayList<String>> newAppointmentInfo = getNewAppointsInfo(xml); // the appointment we are trying to schedule
         String newDate = newAppointmentInfo.get(0).get(0); // date of the new appointment
         String newTime = newAppointmentInfo.get(0).get(1); // time of the new appointment
         String newPhlebID = newAppointmentInfo.get(0).get(2); // phleb of the new appointment
         String newPscID = newAppointmentInfo.get(0).get(3); // psc of the new appointment
         String newPatientID = newAppointmentInfo.get(0).get(4);
         
         newTime += ":00";
         
         if(validateTime(newTime, "07:59:59", "17:00:00")){ // Validate if the appointment is between 8:00 and 5:00
            String sameDateString = getAppointment("Appointment", "apptdate", newDate); // xml string of appoints with the same date as new appointment
            String samePhlebString = getAppointment("Appointment", "phlebid", newPhlebID); // xml string of the appoints with the same phleb as new appointment
            
            if(!samePhlebString.equals("")){ 
               if(!sameDateString.equals("")){
                  ArrayList<ArrayList<String>> sameDateList = getAppointsInfo(sameDateString);    
                  ArrayList<ArrayList<String>> samePhlebsList = getAppointsInfo(samePhlebString);
               
                  if(sameDateList.size() > 0){
                     for(int i=0; i < sameDateList.size(); i++){   // Searching through all of the appointments scheduled on the requested day
                        String appointId1 = sameDateList.get(i).get(2);                     
                        for(int x=0; x<samePhlebsList.size(); x++){ // Searching through all of the appointments scheduled for that phleb
                           String appointId2 = samePhlebsList.get(x).get(2);               
                           if(appointId1.equals(appointId2)){ // if there is an appointment scheduled on that day for that phleb,
                             phlebAppointmentsThatDay.add(sameDateList.get(i));  // add it to the list of appointments scheduled on the requested day for that phleb.
                           }
                        }
                     }
                  }
                              
                  if(phlebAppointmentsThatDay.size() > 0){
                     for(int i=0; i < phlebAppointmentsThatDay.size(); i++){
                        String scheduledStartTime = phlebAppointmentsThatDay.get(i).get(1);  
                        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                        Date scheduledStartTimeDate = null;  
                        
                        try{
                           scheduledStartTimeDate = df.parse(scheduledStartTime); 
                        }
                        catch(ParseException p){
                           System.out.println(p.getMessage());
                           response = "An error has occured in parsing the scheduled start time.";
                        }
                        catch(Exception e){
                           System.out.println(e.getMessage());
                           response = "An error has occured in parsing the scheduled start time.";
                        }
                        
                        Calendar scheduledStartTimeCal = Calendar.getInstance();
                        scheduledStartTimeCal.setTime(scheduledStartTimeDate);
                        String scheduledPsc = phlebAppointmentsThatDay.get(i).get(4);
                        
                        if(newPscID.equals(scheduledPsc)){
                           scheduledStartTimeCal.add(Calendar.MINUTE, 15);// same psc
                        } else {
                           scheduledStartTimeCal.add(Calendar.MINUTE, 45);// different psc
                        }
                        
                        String scheduledEndTime = df.format(scheduledStartTimeCal.getTime());
                        
                        if(!validateTime(newTime, scheduledStartTime, scheduledEndTime)){
                           response = "Appointment can be scheduled.";
                        } else {
                           response = "Time of appointment conflicts with another appointment.";
                           break;
                        } 
                     }
                  } else { // end if there are any appointments that day with the phleb
                     response = "Appointment can be scheduled.";
                  }
               } else { // end if there are any appointments that day with any phleb
                  response = "Appointment can be scheduled.";
               } 
            } else { // If there are no phlebs with that ID
               response = "Appointment can be scheduled.";
            }            
         } else { // end 'if appointment is between 8 and 5 
            response = "Appointment must be between 8:00am and 5:00pm.";
         }        
         
         ArrayList<String> appointmentInfo = new ArrayList<String>();
         
         appointmentInfo.add(response);
         appointmentInfo.add(newDate);
         appointmentInfo.add(newTime);
         appointmentInfo.add(newPhlebID);
         appointmentInfo.add(newPscID);
         appointmentInfo.add(newPatientID);
         
         return appointmentInfo;
   }
   
   
   /*
    * Transforms given object(s) to xml string
    * @param List<Object> objs
    * @param String url
    * @return String appointmentString
    */
   public String transformObjectsToXML(List<Object> objs, String url){
      String appointmentString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><AppointmentList>";
      for (Object obj : objs){
         Appointment appoint = (Appointment)obj;
         Patient patient = appoint.getPatientid();
         Phlebotomist phleb = appoint.getPhlebid();
         PSC psc = appoint.getPscid();
         List<AppointmentLabTest> labTests = appoint.getAppointmentLabTestCollection();
         
         appointmentString += "<appointment date=\"" + appoint.getApptdate() + "\" id=\"" + appoint.getId() + "\" time=\"" + appoint.getAppttime() + "\">";
         appointmentString += "<uri>" +url+ "LAMSService/Appointments/"+appoint.getId()+"</uri>";
         appointmentString += "<patient id=\"" + patient.getId() + "\"><uri/><name>" + patient.getName() + "</name><address>" + patient.getAddress() + "</address><insurance>" + patient.getInsurance() + "</insurance><dob>" + patient.getDateofbirth() + "</dob></patient>";
         appointmentString += "<phlebotomist id=\"" + phleb.getId() + "\"><uri/><name>" + phleb.getName() + "</name></phlebotomist>";
         appointmentString += "<psc id=\"" + psc.getId() + "\"><uri/><name>" + psc.getName() + "</name></psc>";
         
         appointmentString += "<allLabTests>";
            for(AppointmentLabTest test : labTests){
               LabTest lt = test.getLabTest();
               Diagnosis dia = test.getDiagnosis();
               appointmentString += "<appointmentLabTest appointmentId=\"" + appoint.getId() + "\" dxcode=\"" +  dia.getCode() + "\" labTestId=\"" + lt.getId() + "\"><uri/></appointmentLabTest>";
            }
         appointmentString += "</allLabTests>";
         
         appointmentString += "</appointment>";
      }
      appointmentString	+=	"</AppointmentList>";
      return appointmentString;
   }
   
   
   /*
    * Returns the next available appointment ID
    * @return String newID
    */
   public String getNextAppointmentID(){
      List<Object> objs = getAllAppointments();
      
      int objectSize = objs.size();
      Object lastAppointment = objs.get(objectSize-1);
      Appointment appointment = (Appointment)lastAppointment;
      String lastID = appointment.getId();
      //System.out.println(lastID);
      int currentID = Integer.parseInt(lastID);
      int plusOne = currentID+1;
      String newID = Integer.toString(plusOne);
      return newID;
   }


   /**
    * Returns the xml string of lab tests for an appointment
    * @param String xml
    * @return String xmlResponse
    */
   public List<AppointmentLabTest> getLabTests(String xml, String appointmentID){
      xml = xml.trim().replaceAll("[^\\x20-\\x7e\\x0A]", "");
      ArrayList<ArrayList<String>> appointmentsInfo = null;
      NamedNodeMap attributes = null;
      List<AppointmentLabTest> tests = new ArrayList<AppointmentLabTest>();
      
      try {
         appointmentsInfo = new ArrayList<ArrayList<String>>();
         
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         InputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
         Document parsedXML = db.parse(stream);
          
         NodeList allLabTestsList = parsedXML.getElementsByTagName("labTests");
         
         
         for(int i=0; i < allLabTestsList.getLength(); i++){ // will only be 1 element of allLabTests
            Node allTests = allLabTestsList.item(i); // the one allLabTests element          
            NodeList individualTests = allTests.getChildNodes(); // all of the appointmentLabTest elements    
            int childrenCount = individualTests.getLength();
            for (int j = 0; j < childrenCount; j++){
            	Node test = individualTests.item(j);
               attributes = test.getAttributes(); 
               String dxcode = attributes.getNamedItem("dxcode").getNodeValue();
               String labTestId = attributes.getNamedItem("id").getNodeValue();
               
                 AppointmentLabTest testItem = new AppointmentLabTest(appointmentID,labTestId,dxcode);
                 testItem.setDiagnosis((Diagnosis)DBSingleton.getInstance().db.getData("Diagnosis", "code='" + dxcode + "'").get(0));
                 testItem.setLabTest((LabTest)DBSingleton.getInstance().db.getData("LabTest","id='" + labTestId + "'").get(0));
                 tests.add(testItem);
               //xmlResponse += "<appointmentLabTest apptointmentId=\"" + appointmentID + "\" dxcode=\"" + dxcode + "\" labTestId=\"" + labTestId + "\"/>";           
            }
         }
         return tests;
      }
      catch(ParserConfigurationException pce){
         pce.printStackTrace();
         return tests;
      }
      catch(SAXException sax){
         sax.printStackTrace();
         return tests;
      }
      catch(Exception e){
         e.printStackTrace();
         return tests;
      }
   }
   
   
   
   /* 
    * Physically adds the appointment into the DB. 
    * @return boolean success
    */
   public boolean insertAppointment(Appointment newAppt){
      boolean success = DBSingleton.getInstance().db.addData(newAppt);
      return success;
   }



   /**
    * Returns the Patient object requested
    * @param String patientID
    * @return Patient pat
    */
   public Patient getPatient(String patientID){
      List<Object> objs = DBSingleton.getInstance().db.getData("Patient", "ID = '" + patientID + "'");
      Patient pat = null;
      for (Object obj : objs){
         pat = (Patient)obj;
      }
      return pat;
   }
   

   /**
    * Returns the Phlebotomist object requested
    * @param String phlebID
    * @return Phlebotomist phleb
    */
   public Phlebotomist getPhleb(String phlebID){
      List<Object> objs = DBSingleton.getInstance().db.getData("Phlebotomist", "ID = '" + phlebID + "'");
      Phlebotomist phleb = null;
      for (Object obj : objs){
         phleb = (Phlebotomist)obj;
      }
      return phleb;
   }
   

   /**
    * Returns the PSC object requested
    * @param String pscID
    * @return PSC psc
    */
   public PSC getPsc(String pscID){
      List<Object> objs = DBSingleton.getInstance().db.getData("PSC", "ID = '" + pscID + "'");
      PSC ps = null;
      for (Object obj : objs){
         ps = (PSC)obj;
      }
      return ps;
   }
} // end BusinessLayer
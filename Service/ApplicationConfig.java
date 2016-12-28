package Service;

import java.util.Set;
import javax.ws.rs.core.Application;

@javax.ws.rs.ApplicationPath("resources") // path will be the domain/name of war file/whatever is in this string
public class ApplicationConfig extends Application {

   @Override
   public Set<Class<?>> getClasses(){
      return getRestResourceClasses();
   }
   
   private Set<Class<?>> getRestResourceClasses(){
      Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
      resources.add(LAMSService.class);
      return resources;
   }

} // end ApplicationConfig

package rentalservice.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="reclamation", url="http://localhost:8083")
public interface ReclamationService {

    @RequestMapping(method= RequestMethod.POST, path="/reclamations")
    public void reclamationed(@RequestBody Reclamation reclamation);

}
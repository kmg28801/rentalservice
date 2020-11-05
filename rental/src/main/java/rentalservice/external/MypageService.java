package rentalservice.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="mypage", url="http://localhost:8084")
public interface MypageService {

    @RequestMapping(method= RequestMethod.POST, path="/mypages")
    public void deletemypage(@RequestBody Mypage mypage);

}
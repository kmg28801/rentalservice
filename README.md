
# 도서 대여시스템

본 프로그램은 도서 대여 시스템입니다.
- 체크포인트 : https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW


# Table of contents

- [도서대여시스템](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)
  - [신규 개발 조직의 추가](#신규-개발-조직의-추가)

# 서비스 시나리오

기능적 요구사항
1. 고객이 책을 대여한다.
2. 대여를 하면 결제 기능이 호출된다.
3. 결제가 완료되면 고객의 대여상태를 업데이트한다.
4. 고객이 책을 반납한다.
5. 반납하게되면 반납 기능이 호출된다.
6. 반납이 완료되면 고객의 대여상태를 업데이트한다.
7. 고객은 자신의 대여상태를 확인할 수 있다.

비기능
	1. 
		- 결제가 완료되지 않은 대여건은 대여가 성립되지 않는다. (Sync 호출)
		- 반납이 완료되지 않은 대여건은 반납이 성립되지 않는다. (Sync 호출)
	2. 장애격리
		- 대여는 365일 24시간 받을 수 있어야 한다
		- 대여시스템이 과중되면 고객은 잠시동안 대여를 하지 못하고, 대여를 잠시후에 하도록 유도한다. 
			(fallback, circuit breaker)
	3. 성능
		- 고객이 자신의 대여상태를 자주 확인할 수 있다.(CQRS)





# 체크포인트

- 분석 설계


  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
    - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?
- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
    - 모니터링, 앨럿팅: 
  - 무정지 운영 CI/CD (10)
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 
    - Contract Test :  자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?


# 분석/설계


## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:  http://www.msaez.io/#/storming/mE5NnEwVhiZ6GAhrFPdKZIyqntJ2/local/d1c70368956952658ad59f5d08751f08/init


### 이벤트 도출
![image](https://user-images.githubusercontent.com/53685313/98325271-bf4e5400-2031-11eb-9c33-11dfbdd072f9.png)


    - 도메인 서열 분리 
        - Core Domain:  order, delivery : 안될 핵심 서비스이며, 연견 Up-time SLA 수준을 99.999% 목표, 배포주기는 order 의 경우 1주일 1회 미만, delivery 의 경우 1개월 1회 미만
        - Supporting Domain: 경쟁력을 내기위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함.
        - General Domain:   pay : 결제서비스로 3rd Party 외부 서비스를 사용하는 것이 경쟁력이 높음 (핑크색으로 이후 전환할 예정)


### 기능적/비기능적 요구사항을 커버하는지 검증

![image](https://user-images.githubusercontent.com/53685313/98325403-0f2d1b00-2032-11eb-810e-d81cd2881ac8.png)


    - 고객이  대여한다 (ok)
    - 대여를 하면 결제 기능이 호출된다 (ok)
    - 고객이 반납한다 (ok)
    - 반납을 하게되면 도서가 반납된다 (ok)
    - 고객이 주문상태를 확인한다 (ok)
    
### 비기능 요구사항에 대한 검증

![image](https://user-images.githubusercontent.com/53685313/98325551-6a5f0d80-2032-11eb-8351-96c87f26efc3.png)


      1 결제가 되지 않은 대여건은 대여가 성립되지 않아야 한다  Sync 호출 (ok)
      2 반납이 완료 처리된 건에 대해서만 대여건이 삭제된다. Sync 호출 (ok)

## 헥사고날 아키텍처 다이어그램 도출
    
![image](https://user-images.githubusercontent.com/53685313/98326368-6c29d080-2034-11eb-8c76-af6802e16594.png)

    - PolyGlot 패턴을 적용해 H2, HSQL 2개 DB 사용
    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

```
cd gateway
mvn spring-boot:run

cd rental
mvn spring-boot:run 

cd pay
mvn spring-boot:run  

cd mypage
mvn spring-boot:run  

cd reclamation
mvn spring-boot:run  


## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 order 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력하였고 영문으로 사용하여 별다른 오류 없이 구현하였다.

```
package rentalservice;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Rental_table")
public class Rental {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String productId;
    private Integer qty;
    private String status;

    @PostPersist
    public void onPostPersist() {

        Renting renting = new Renting();
        BeanUtils.copyProperties(this, renting);
        renting.publishAfterCommit();
        rentalservice.external.Pay pay = new rentalservice.external.Pay();
        pay.setOrderId(renting.getId());
        pay.setStatus("Payed");
        RentalApplication.applicationContext.getBean(rentalservice.external.PayService.class)
                .payment(pay);
    }
    @PreRemove
    public void onPreRemove() {
        Reclamationing reclamationing = new Reclamationing();
        BeanUtils.copyProperties(this, reclamationing);
        reclamationing.publishAfterCommit();

        rentalservice.external.Mypage mypage = new rentalservice.external.Mypage();
        mypage.setOrderId(reclamationing.getId());
        mypage.setStatus(reclamationing.getStatus());
        mypage.setProductId(reclamationing.getProductId());
        mypage.setQty(reclamationing.getQty());
        RentalApplication.applicationContext.getBean(rentalservice.external.MypageService.class)
                .deletemypage(mypage);

        rentalservice.external.Reclamation reclamation = new rentalservice.external.Reclamation();
        reclamation.setStatus("Reclaiming");
        reclamation.setOrderId(reclamationing.getId());

        RentalApplication.applicationContext.getBean(rentalservice.external.ReclamationService.class)
            .reclamationed(reclamation);





    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}

```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package rentalservice;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface RentalRepository extends PagingAndSortingRepository<Rental, Long>{


}

```
- 적용 후 REST API 의 테스트
```
# rental 서비스의 주문처리
http POST http://20.196.129.196:8080/rentals productId=taejoong qty=10

![image](https://user-images.githubusercontent.com/53685313/98326489-bf9c1e80-2034-11eb-807c-838526836877.png)



# rental 서비스의 주문취소 처리
http DELETE http://20.196.129.196:8080/rentals/2

![image](https://user-images.githubusercontent.com/53685313/98326548-de9ab080-2034-11eb-972b-13f2b1fe5227.png)



# 대여 상태 확인
http GET http://20.196.129.196:8080/rentals/1

![image](https://user-images.githubusercontent.com/53685313/98326604-f3774400-2034-11eb-8f45-612aa532a8ed.png)
```

## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 주문(order)->결제(pay) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 결제서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
# (order) PayService.java


package rentalservice.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="pay", url="${api.pay.url}")
public interface PayService {

    @RequestMapping(method= RequestMethod.POST, path="/pays")
    public void payment(@RequestBody Pay pay);

}
```

- 대여가 들어온 직후(@PostPersist) 결제를 요청하도록 처리
```

# Rental.java (Entity)

     @PostPersist
    public void onPostPersist() {

        Renting renting = new Renting();
        BeanUtils.copyProperties(this, renting);
        renting.publishAfterCommit();
        rentalservice.external.Pay pay = new rentalservice.external.Pay();
        pay.setOrderId(renting.getId());
        pay.setStatus("Payed");
        RentalApplication.applicationContext.getBean(rentalservice.external.PayService.class)
                .payment(pay);
    }
    ```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 결제 시스템이 장애가 나면 주문도 못받는다는 것을 확인:




## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


결제가 이루어진 후에 렌탈서비스에서는 자신의 status를 확인할 수 있는 행위는 동기식이 아니라 비 동기식으로 처리하였다.
 
- 이를 위하여 결제이력에 기록을 남긴 후에 곧바로 결제승인이 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
package rentalservice;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Pay_table")
public class Pay {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private Integer chargeAmount;
    private String status;

    @PostPersist
    public void onPostPersist(){
        PayConfirmed payConfirmed = new PayConfirmed();
        BeanUtils.copyProperties(this, payConfirmed);
        payConfirmed.publishAfterCommit();


    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    public Integer getChargeAmount() {
        return chargeAmount;
    }

    public void setChargeAmount(Integer chargeAmount) {
        this.chargeAmount = chargeAmount;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}


```
- 렌탈 서비스에서는 결제승인 및 반납 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
package rentalservice;

import rentalservice.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayConfirmed_UpdateStatus(@Payload PayConfirmed payConfirmed){

        if(payConfirmed.isMe()){
            System.out.println("##### listener UpdateStatus : " + payConfirmed.toJson());
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReclamationConfirmed_UpdateStatus(@Payload ReclamationConfirmed reclamationConfirmed){

        if(reclamationConfirmed.isMe()){
            System.out.println("##### listener UpdateStatus : " + reclamationConfirmed.toJson());
        }
    }

}


```



# CQRS 적용
주문된 현황을 view로 구현함.

![image](https://user-images.githubusercontent.com/53685313/98326889-a6e03880-2035-11eb-97f3-4db4e2683619.png)



# gateway 적용
소스적용 (istio-gateway)

apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: rental-gateway
  namespace: rental
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - rental/*

소스적용 (istio-virtual-service)
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: dap-virtual-service
  namespace: rental
spec:
  hosts:
  - "*"
  gateways:
  - dap-gateway
  http:
  - match:
    - uri:
        prefix: /auth
    route:
    - destination:
        host: dap-auth-service
        port:
          number: 3006
  - match:
    - uri:
        prefix: /api/users
    - uri:
        prefix: /api/texts       
    - uri:
        prefix: /api/bot 
    route:
    - destination:
        host: dap-user-service
        port:
          number: 80
  - match:
    - uri:
        prefix: /api/assets
    route:
    - destination:
        host: dap-asset-service
        port:
          number: 80         
  - match:
    - uri:
        prefix: /api/dashboard
    - uri:
        prefix: /api/analysis
    - uri:
        prefix: /api/myasset
    route:
    - destination:
        host: dap-dashboard-service
        port:
          number: 80         
  - match:
    - uri:
        prefix: /api/contents
    route:
    - destination:
        host: dap-contents-service
        port:
          number: 80             
  - match:
    - uri:
        prefix: /api/contact
    route:
    - destination:
        host: dap-contact-service
        port:
          number: 80             
  - match:
    - uri:
        prefix: /api/admin/user
    - uri:
        prefix: /api/admin/statics
    - uri:
        prefix: /api/admin/bot
    route:
    - destination:
        host: dap-admin-user-service
        port:
          number: 80
  - match:
    - uri:
        prefix: /api/admin/contact
    route:
    - destination:
        host: dap-admin-contact-service
        port:
          number: 80

호출확인(rental)
<div>
<img width="250" src="https://user-images.githubusercontent.com/30397679/98194005-c574ff80-1f61-11eb-897b-a630c5abea71.PNG"/>
</div>
호출확인(pay)
<div>
![image](https://user-images.githubusercontent.com/53685313/98326999-ec9d0100-2035-11eb-90e2-dc64a86667a1.png)

</div>
호출확인(reclamation)
<div>
<img width="250" src="https://user-images.githubusercontent.com/30397679/98194020-cb6ae080-1f61-11eb-9d87-bce9e0482aee.PNG"/>
</div>
호출확인(mypage)
<div>
<img width="250" src="https://user-images.githubusercontent.com/30397679/98194015-ca39b380-1f61-11eb-8d07-ba6a349aa52d.PNG"/>
</div>


각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 Azure를 사용하였으며, pipeline build script 는 각 프로젝트 폴더 이하에 deployment.yml, service.yml 에 포함되었다.


## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

시나리오는 접수(request)-->결제(payment) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 결제 요청이 과도할 경우 CB 를 통하여 장애격리.

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 680 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
```
# application.yml
feign:
  hystrix:
    enabled: true

hystrix:
  command:
    default:
      execution.isolation.thread.timeoutInMilliseconds: 600

```

- 피호출 서비스(결제:pay) 의 임의 부하 처리 - 800 밀리에서 증감 300 밀리 정도 왔다갔다 하게
```
# (pay)pay.java (Entity)

    @PostPersist
    public void onPostPersist(){
        Payment payment = new Payment();
        BeanUtils.copyProperties(this, paid);
        payment.publishAfterCommit();

        try {
            Thread.sleep((long) (800 + Math.random() * 300));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
```

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 1명
- 10초 동안 실시
![image](https://user-images.githubusercontent.com/68535067/97244569-66660b00-183c-11eb-9b7e-cda86b0d59ac.png)

- 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여줌. 하지만, 81.82% 가 성공하였고, 18.18%가 실패했다는 것은 고객 사용성에 있어 좋지 않기 때문에 Retry 설정과 동적 Scale out (replica의 자동적 추가,HPA) 을 통하여 시스템을 확장 해주는 후속처리가 필요.

- Availability 가 높아진 것을 확인 (siege)

### 오토스케일 아웃
앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다. 


- 결제서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다:
```
kubectl autoscale deploy rental --min=1 --max=10 --cpu-percent=15
```

![image](https://user-images.githubusercontent.com/53685313/98327195-574e3c80-2036-11eb-958b-9c018dfc2582.png)


- CB 에서 했던 방식대로 워크로드를 2분 동안 걸어준다.
```
siege -c100 -t120S -r10 -v --content-type "application/json" 'http://20.196.129.196:8080/rentals POST {"productId": "taejoong", "qty":5}'

```
- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy pay -w
```
- 어느정도 시간이 흐른 후 (약 30초) 스케일 아웃이 벌어지는 것을 확인할 수 있다:
![image](https://user-images.githubusercontent.com/53685313/98327251-7b118280-2036-11eb-94ce-cee00ad945fe.png)



## Configmap
- configmap.yaml 파일설정

apiVersion: v1
kind: ConfigMap
metadata:
  name: apiurl
data:
  url: http://pay:8080
  fluented-sever-ip: 10.xxx.xxx.xxx

- deployment.yaml파일 설정
apiVersion: apps/v1
kind: Deployment
metadata:
  name: rental
  labels:
    app: rental
spec:
  replicas: 1
  selector:
    matchLabels:
      app: rental
  template:
    metadata:
      labels:
        app: rental
    spec:
      containers:
        - name: rental
          image: username/rental:latest
          ports:
            - containerPort: 8080
          env:
            - name: configurl
              valueFrom:
                configMapKeyRef:
                  name: apiurl
                  key: url
          readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
          livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5

- application.yaml 파일 설정
server:
  port: 8080
---

spring:
  profiles: default
  jpa:
    properties:
      hibernate:
        show_sql: true
        format_sql: true
  cloud:
    stream:
      kafka:
        binder:
          brokers: localhost:9092
        streams:
          binder:
            configuration:
              default:
                key:
                  serde: org.apache.kafka.common.serialization.Serdes$StringSerde
                value:
                  serde: org.apache.kafka.common.serialization.Serdes$StringSerde
      bindings:
        event-in:
          group: rental
          destination: rentalservice
          contentType: application/json
        event-out:
          destination: rentalservice
          contentType: application/json

logging:
  level:
    org.hibernate.type: trace
    org.springframework.cloud: debug
server:
  port: 8081


api:
  pay:
    url: http://localhost:8082
  mypage:
    url: http://localhost:8084
  reclamation:
    url: http://localhost:8083
---

spring:
  profiles: docker
  cloud:
    stream:
      kafka:
        binder:
          brokers: my-kafka.kafka.svc.cluster.local:9092
        streams:
          binder:
            configuration:
              default:
                key:
                  serde: org.apache.kafka.common.serialization.Serdes$StringSerde
                value:
                  serde: org.apache.kafka.common.serialization.Serdes$StringSerde
      bindings:
        event-in:
          group: rental
          destination: rentalservice
          contentType: application/json
        event-out:
          destination: rentalservice
          contentType: application/json

api:
  pay:
    url: ${apiurl}
  mypage:
    url: http://mypage:8080
  reclamation:
    url: http://reclamation:8080


- CancellationService 파일 설정

package rentalservice.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="pay", url="${api.pay.url}")
public interface PayService {

    @RequestMapping(method= RequestMethod.POST, path="/pays")
    public void payment(@RequestBody Pay pay);

}



- 8080포트로 설정하여 테스트
![image](https://user-images.githubusercontent.com/53685313/98327424-c88def80-2036-11eb-8bf4-694dd52fe177.png)


## Polyglot 구현
- Pay의 DB를 hsql로 변경한다.
![image](https://user-images.githubusercontent.com/53685313/98327508-0559e680-2037-11eb-8256-cdafd8e0821c.png)

- Intellij에서 Dependency에 hsql이 추가됨을 확인한다.
![image](https://user-images.githubusercontent.com/53685313/98327560-24f10f00-2037-11eb-9c09-18051773d1d2.png)


## Livness구현
- Rental의 depolyment.yaml 소스설정
- http get방식에서 tcp방식 포트 40001로 변경하여 pod describe


![image](https://user-images.githubusercontent.com/53685313/98198848-4b4a7800-1f6d-11eb-9005-00302ec2dfa1.png)


![image](https://user-images.githubusercontent.com/53685313/98198470-5fda4080-1f6c-11eb-8f10-3c8902364ad6.png)




- tcp 8080 변경 후 deploy 재시작 이후 describe 확인

![image](https://user-images.githubusercontent.com/53685313/98198613-afb90780-1f6c-11eb-96d1-798a86c935df.png)
![image](https://user-images.githubusercontent.com/53685313/98198740-04f51900-1f6d-11eb-8d09-e9b978a8f8d0.png)




- 원복후 정상 확인
![image](https://user-images.githubusercontent.com/53685313/98197525-5059f800-1f6a-11eb-9bca-8157c21c6451.png)


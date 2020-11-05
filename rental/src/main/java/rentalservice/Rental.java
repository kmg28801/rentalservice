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
        mypage.setId(reclamationing.getId());
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

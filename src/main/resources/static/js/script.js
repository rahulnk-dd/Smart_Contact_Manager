console.log("Welcome to Smart Contact Manager Project");

const toggleSidebar=()=>{

    if($('.sidebar').is(":visible")) {
        // true
        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");
    }else{
        // false
        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");
    }
};


const search=()=>{
    // console.log("searching....");

    let query = $("#search-input").val();

    if(query == ""){
        $(".search-result").hide();
    }else{
        console.log(query);
        // sending request to server

        let url= `http://localhost:8080/search/${query}`;

        fetch(url).then((response) =>{
            return response.json();
        })
        .then((data)=>{
              console.log(data);

              let text=` <div class='list-group'> `;

              data.forEach((contact)=>{
                text+=`<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'> ${contact.name} </a>`
              })

              text+='</div>';

              $(".search-result").html(text);
              $(".search-result").show();
        });


        
    }
};


// first request to server to create order

const paymentStart=()=>{
    console.log("Payment Start");

    let amount=$("#payment_amount").val();
    console.log(amount);

    if (amount==null || amount=='') {
        // alert("Amount is required..");
        swal("Failed!", "Amount is required..", "error");
        return;
    }

    
    
    // code
    // using ajax to send request to server 
    $.ajax({
            url: "/user/create_order",
            data: JSON.stringify({amount:amount, info:"order_request"}),
            contentType:"application/json",
            type: "POST",
            dataType: "json",
            success: function(response){
                // invoked when success
                console.log(response);

                if(response.status == "created"){
                    // open payment form 
                    let options={
                        key:'rzp_test_Gwwm4AGXR3j17G',
                        amount:response.amount,
                        currency:'INR',
                        name:'Smart Contact Manager',
                        description:'Donation',
                        image:"https://example.com/your_logo",
                        order_id:response.id,
                        handler:function(response){
                            console.log(response.razorpay_payment_id);
                            console.log(response.razorpay_order_id);
                            console.log(response.razorpay_signature);
                            console.log('Payment Successfull');
                            // alert("Payment Successfull");

                            updatePaymentOnServer(
                                response.razorpay_payment_id,
                                response.razorpay_order_id,
                                "paid"
                            );

                            // swal("Good job!", "Payment Successfull", "success");
                        },
                        prefill: {
                            name: "",
                            email: "",
                            contact: ""
                        },
                        notes: {
                            address: "SCM Corporate Office"
                        },
                        theme: {
                            color: "#3399cc"
                        }
                    };

                    let rzp= new Razorpay(options);

                    rzp.on('payment.failed', function (response){
                        console.log(response.error.code);
                        console.log(response.error.description);
                        console.log(response.error.source);
                        console.log(response.error.step);
                        console.log(response.error.reason);
                        console.log(response.error.metadata.order_id);
                        console.log(response.error.metadata.payment_id);
                        // alert("Payment Failed");
                        swal("Failed!", "Payment Failed", "error");
                });

                    rzp.open();
                }

            },
            error: function(error){
                // invoked when error
                console.log(error);
                alert("Something went wrong...");
            }
        }

    )
};


function updatePaymentOnServer(payment_id,order_id,status)
{

    $.ajax({
            url: "/user/update_order",
            data: JSON.stringify({payment_id:payment_id, order_id:order_id, status:status,}),
            contentType:"application/json",
            type: "POST",
            dataType: "json",
            success: function(response){
                swal("Good job!", "Payment Successfull", "success");
            },
            error: function(error){
                swal("Failed!", "You Payment Failed, we didn't get on server, we will contact you soon", "error");
            }
    })

}
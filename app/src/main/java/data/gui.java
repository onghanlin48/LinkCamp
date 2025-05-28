package data;

public class gui {
    public String otp_t = "LinkCamp Verify OTP";
    public String otp_t_r = "Resend LinkCamp Verify OTP";
    public String d_t = "You have send a Donation";
    public String d_t_r = "You have receive a Donation";
    public String otp_content(String number){

        return "        <body style='font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background-color: #f0f0f0;'>" +
                "            <div style='text-align: center; border: 1px solid #ccc; padding: 20px; background-color: #fff; border-radius: 8px; width: 300px;'>" +
                "                <h1>LinkCamp</h1>" +
                "                <p>Please check the OTP code for email verification</p>" +
                "                <p>OTP Code</p>" +
                "                <div style='font-size: 24px; font-weight: bold; color: #123456; padding: 10px; background-color: #cce0ff; border-radius: 4px; margin-top: 10px;'>"+number+"</div>" +
                "            </div>" +
                "        </body>" ;
    }
    public String donation_content(String number,String name,String id){

        return "        <body style='font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background-color: #f0f0f0;'>" +
                "            <div style='text-align: center; border: 1px solid #ccc; padding: 20px; background-color: #fff; border-radius: 8px; width: 300px;'>" +
                "                <h1>LinkCamp</h1>" +
                "                <p>Receipt number :"+id+"</p>" +
                "                <p>You have send donation to "+name+"</p>" +
                "                <p>Amount</p>" +
                "                <div style='font-size: 24px; font-weight: bold; color: #123456; padding: 10px; background-color: #cce0ff; border-radius: 4px; margin-top: 10px;'>RM "+number+"</div>" +
                "            </div>" +
                "        </body>" ;
    }

    public String donation_content_receive(String number,String name,String id){

        return "        <body style='font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background-color: #f0f0f0;'>" +
                "            <div style='text-align: center; border: 1px solid #ccc; padding: 20px; background-color: #fff; border-radius: 8px; width: 300px;'>" +
                "                <h1>LinkCamp</h1>" +
                "                <p>Receipt number :"+id+"</p>" +
                "                <p>You have received donation</p>" +
                "                <p>Amount</p>" +
                "                <div style='font-size: 24px; font-weight: bold; color: #123456; padding: 10px; background-color: #cce0ff; border-radius: 4px; margin-top: 10px;'>RM "+number+"</div>" +
                "            </div>" +
                "        </body>" ;
    }
    public String otp_new(String number){

        return "        <body style='font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background-color: #f0f0f0;'>" +
                "            <div style='text-align: center; border: 1px solid #ccc; padding: 20px; background-color: #fff; border-radius: 8px; width: 300px;'>" +
                "                <h1>LinkCamp</h1>" +
                "                <p>Please check the OTP code for new email verification</p>" +
                "                <p>OTP Code</p>" +
                "                <div style='font-size: 24px; font-weight: bold; color: #123456; padding: 10px; background-color: #cce0ff; border-radius: 4px; margin-top: 10px;'>"+number+"</div>" +
                "            </div>" +
                "        </body>" ;
    }

    public String otp_v(String number){

        return "        <body style='font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background-color: #f0f0f0;'>" +
                "            <div style='text-align: center; border: 1px solid #ccc; padding: 20px; background-color: #fff; border-radius: 8px; width: 300px;'>" +
                "                <h1>LinkCamp</h1>" +
                "                <p>Please check the OTP code for verification</p>" +
                "                <p>OTP Code</p>" +
                "                <div style='font-size: 24px; font-weight: bold; color: #123456; padding: 10px; background-color: #cce0ff; border-radius: 4px; margin-top: 10px;'>"+number+"</div>" +
                "            </div>" +
                "        </body>" ;
    }


    public String otp_content_reset(String number){

        return "        <body style='font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background-color: #f0f0f0;'>" +
                "            <div style='text-align: center; border: 1px solid #ccc; padding: 20px; background-color: #fff; border-radius: 8px; width: 300px;'>" +
                "                <h1>LinkCamp</h1>" +
                "                <p>Please check the OTP code for reset password verification</p>" +
                "                <p>OTP Code</p>" +
                "                <div style='font-size: 24px; font-weight: bold; color: #123456; padding: 10px; background-color: #cce0ff; border-radius: 4px; margin-top: 10px;'>"+number+"</div>" +
                "            </div>" +
                "        </body>" ;
    }

    public String otp_content_login(String number){

        return "        <body style='font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background-color: #f0f0f0;'>" +
                "            <div style='text-align: center; border: 1px solid #ccc; padding: 20px; background-color: #fff; border-radius: 8px; width: 300px;'>" +
                "                <h1>LinkCamp</h1>" +
                "                <p>Please check the OTP code for Login verification</p>" +
                "                <p>OTP Code</p>" +
                "                <div style='font-size: 24px; font-weight: bold; color: #123456; padding: 10px; background-color: #cce0ff; border-radius: 4px; margin-top: 10px;'>"+number+"</div>" +
                "            </div>" +
                "        </body>" ;
    }
    public String workshop(String Content, String workshop_name, String name) {
        return "<body style='font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f4f4f4;'>" +
                "  <div style='max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;'>" +
                "    <div style='background-color: #00b7ff; color: #ffffff; padding: 10px; border-radius: 8px 8px 0 0; text-align: center;'>" +
                "      <img src='https://drive.usercontent.google.com/download?id=1bzEHvqctFJ3J1wsU8GMfvHyP-Hnwgjdc&export=download&authuser=0&confirm=t&uuid=48be36fb-3b5c-403a-97bd-4af9426b1865&at=APvzH3p1nDrM4mXv-wSLQlkFgjLx:1735094072802' alt='LinkCamp' style='max-height: 40px;'>" +
                "    </div>" +
                "    <br>" +
                "       <b>" + workshop_name + "<b> has sent you a message." +
                "    <br>" +
                "    <br>" +
                "    <div style='background-color: #e0e0e0; padding: 20px;'>" +
                "     " + Content +
                "    </div>" +
                "    <div style='font-size: 12px; color: #777777; text-align: center; margin-top: 20px;'>" +
                "      Please note: This email send to " + name +
                "    </div>" +
                "  </div>" +
                "</body>" ;
    }

    public String workshop_title(String workshop_title,String name){
        return name+" has responded to your register for Workshop " + workshop_title;
    }

    public String work_title(String work_title,String name){
        return name+" has responded to your apply job for " + work_title;
    }

}

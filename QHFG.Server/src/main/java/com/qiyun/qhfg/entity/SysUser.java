package com.qiyun.qhfg.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@SuppressWarnings("serial")
@Data
public class SysUser {
    @TableId(value = "\"id\"")
    private Integer id;

    private String userName;

    private String passWord;

    private String fullName;

    private String phone;

    private String email;

    private String company;

    private String region;

    private String role;

    private Boolean isDeleted;

    private Date createDate;

    public String toString() {
        return "{\"Id\":" + this.id + "," +
                "\"UserName\":\""+this.userName+"\"," +
                "\"Password\":\""+this.passWord+"\"," +
                "\"Role\":\""+this.role+"\"," +
                "\"Name\":\""+this.userName+"\"," +
                "\"Phone\":"+this.phone+"," +
                "\"Company\":"+this.company+"}";
    }
}



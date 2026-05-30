package com.reeya.payment_risk_engine.model.api;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class IpGeoLocationResponse {

    private String success;
    private String country;

}

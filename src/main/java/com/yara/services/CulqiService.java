package com.yara.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class CulqiService {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    CulqiService.class
            );

    @Value("${culqi.secret-key}")
    private String secretKey;

    private final RestTemplate
            restTemplate;

    public CulqiService(
            RestTemplate restTemplate
    ) {

        this.restTemplate =
                restTemplate;
    }

    public String cobrarTarjeta(

            String token,

            BigDecimal monto
    ) {

        String url =
                "https://api.culqi.com/v2/charges";

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        headers.set(
                "Authorization",
                "Bearer " + secretKey
        );

        Map<String, Object> body =
                new HashMap<>();

        body.put(
                "amount",
                monto.multiply(
                        BigDecimal.valueOf(100)
                ).intValue()
        );

        body.put("currency_code", "PEN");

        body.put("email",
                "test@culqi.com"
        );

        body.put("source_id", token);

        HttpEntity<Map<String, Object>>
                request =
                new HttpEntity<>(
                        body,
                        headers
                );

        try {

            Map<String, Object> response =
                    restTemplate.postForObject(
                            url,
                            request,
                            Map.class
                    );

            logger.info(
                    "Charge generado correctamente: {}",
                    response.get("id")
            );

            return response.get("id")
                    .toString();

        } catch (Exception e) {

            logger.error(
                    "Error procesando pago en Culqi",
                    e
            );

            throw e;
        }
    }

    public String crearCustomer(

            String email
    ) {

        String url =
                "https://api.culqi.com/v2/customers";

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        headers.set(
                "Authorization",
                "Bearer " + secretKey
        );

        Map<String, Object> body =
                new HashMap<>();

        body.put(
                "email",
                email
        );


        body.put(
                "first_name",
                "Usuario"
        );

        body.put(
                "last_name",
                "Yara"
        );

        body.put(
                "address",
                "Av Javier Prado 123"
        );

        body.put(
                "address_city",
                "Lima"
        );

        body.put(
                "country_code",
                "PE"
        );

        body.put(
                "phone_number",
                "999999999"
        );

        HttpEntity<Map<String, Object>>
                request =
                new HttpEntity<>(
                        body,
                        headers
                );

        Map response =
                restTemplate.postForObject(
                        url,
                        request,
                        Map.class
                );

        return response.get("id")
                .toString();
    }

    public Map<String, Object>
    crearCard(

            String customerId,

            String token
    ) {

        String url =
                "https://api.culqi.com/v2/cards";

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        headers.set(
                "Authorization",
                "Bearer " + secretKey
        );

        Map<String, Object> body =
                new HashMap<>();

        body.put(
                "customer_id",
                customerId
        );

        body.put(
                "token_id",
                token
        );

        HttpEntity<Map<String, Object>>
                request =
                new HttpEntity<>(
                        body,
                        headers
                );

        return restTemplate.postForObject(
                url,
                request,
                Map.class
        );
    }
}

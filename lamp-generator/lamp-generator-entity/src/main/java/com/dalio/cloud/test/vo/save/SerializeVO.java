package com.dalio.cloud.test.vo.save;


import com.dalio.basic.annotation.echo.Echo;
import com.dalio.basic.interfaces.echo.EchoVO;
import com.dalio.cloud.model.enumeration.Sex;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@ToString
public class SerializeVO implements EchoVO, Serializable {
    private Map<String, Object> echoMap = new HashMap<>();

    /**
     * 支持6种格式
     */
    private LocalDateTime localDateTime;

    private Date date;
    private LocalDate localDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalTime localTime;

    private Long lon;
    private BigInteger bi;
    private BigDecimal bd;

    @Echo(api = Echo.ENUM_API)
    private Sex sex;

}

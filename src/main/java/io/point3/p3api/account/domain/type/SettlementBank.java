package io.point3.p3api.account.domain.type;

import java.util.Arrays;
import java.util.Optional;

public enum SettlementBank {
  KDB_INDUSTRIAL_BANK("002", "KDB산업은행"),
  IBK_INDUSTRIAL_BANK("003", "IBK기업은행"),
  KB_KOOKMIN_BANK("004", "KB국민은행"),
  SUHYUP_BANK("007", "Sh수협은행"),
  NH_NONGHYUP_BANK("011", "NH농협은행"),
  WOORI_BANK("020", "우리은행"),
  SC_FIRST_BANK("023", "SC제일은행"),
  CITI_BANK("027", "한국씨티은행"),
  IM_BANK("031", "iM뱅크"),
  BUSAN_BANK("032", "부산은행"),
  GWANGJU_BANK("034", "광주은행"),
  JEJU_BANK("035", "제주은행"),
  JEONBUK_BANK("037", "전북은행"),
  GYEONGNAM_BANK("039", "경남은행"),
  SAEMAUL_GEUMGO("045", "새마을금고"),
  CREDIT_UNION("048", "신협"),
  SAVINGS_BANK("050", "저축은행"),
  FORESTRY_COOPERATIVE("064", "산림조합"),
  KOREA_POST("071", "우체국"),
  HANA_BANK("081", "하나은행"),
  SHINHAN_BANK("088", "신한은행"),
  K_BANK("089", "케이뱅크"),
  KAKAO_BANK("090", "카카오뱅크"),
  TOSS_BANK("092", "토스뱅크");

  private final String code;
  private final String displayName;

  SettlementBank(String code, String displayName) {
    this.code = code;
    this.displayName = displayName;
  }

  public String code() {
    return code;
  }

  public String displayName() {
    return displayName;
  }

  public static Optional<SettlementBank> findByCode(String code) {
    return Arrays.stream(values()).filter(bank -> bank.code.equals(code)).findFirst();
  }
}

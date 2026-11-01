package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

public class CompanyDto {
    @SerializedName("company_id")
    private int companyId;

    @SerializedName("name")
    private String name;

    public int getCompanyId() { return companyId; }
    public String getName() { return name; }
}

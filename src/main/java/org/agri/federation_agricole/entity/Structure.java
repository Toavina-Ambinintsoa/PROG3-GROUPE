package org.agri.federation_agricole.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Structure {
    private Member PRESIDENT;
    private Member VICE_PRESIDENT;
    private Member SECRETARY;
    private Member TREASURER;
}

package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Objects;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "app_document")
public class AppDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String TelegramFileId;

    private String docName;

    @OneToOne
    private BinaryContent binaryContent;

    private String mimeType;

    private Long fileSize;

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || Hibernate.getClass(this) != Hibernate.getClass(o))return false;
        AppDocument that = (AppDocument) o;
        return TelegramFileId != null && Objects.equals(TelegramFileId , that.TelegramFileId);
    }
    @Override
    public int hashCode(){

        return getClass().hashCode();
    }

}

package st.old.isis.extraction.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table
@Entity
public class Shedlock {

  @Id
  @Column(length = 64)
  private String name;

  @Column(columnDefinition = "DATETIME")
  private Integer lockUntil;

  @Column(columnDefinition = "DATETIME")
  private Integer lockedAt;

  private String lockedBy;
}

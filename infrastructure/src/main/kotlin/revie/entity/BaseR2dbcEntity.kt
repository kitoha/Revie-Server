package revie.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime

abstract class BaseR2dbcEntity(
  @CreatedDate
  @Column("created_at")
  var createdAt: LocalDateTime? = null,

  @LastModifiedDate
  @Column("updated_at")
  var updatedAt: LocalDateTime? = null,

  @Column("deleted_at")
  var deletedAt: LocalDateTime? = null
) {

  fun delete() {
    this.deletedAt = LocalDateTime.now()
  }

  val isDeleted: Boolean
    get() = deletedAt != null
}
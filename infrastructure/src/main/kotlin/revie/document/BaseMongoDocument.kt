package revie.document

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

abstract class BaseMongoDocument(
  @CreatedDate
  @Field("created_at")
  var createdAt: LocalDateTime? = null,

  @LastModifiedDate
  @Field("updated_at")
  var updatedAt: LocalDateTime? = null,

  @Field("deleted_at")
  var deletedAt: LocalDateTime? = null
) {
  fun delete() {
    this.deletedAt = LocalDateTime.now()
  }

  val isDeleted: Boolean
    get() = deletedAt != null
}
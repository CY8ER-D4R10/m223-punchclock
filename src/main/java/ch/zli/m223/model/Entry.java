package ch.zli.m223.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Ein Zeiteintrag.
 *
 * Trägt beide Beziehungen als <b>besitzende Seite</b>:
 * <ul>
 *   <li>{@code category} — 1:n, der Fremdschlüssel {@code category_id} liegt
 *       in dieser Tabelle. Standardmässig <b>eager</b> geladen (ToOne).</li>
 *   <li>{@code tags} — n:m über die Zwischentabelle {@code entry_tag}.
 *       Standardmässig <b>lazy</b> geladen (ToMany).</li>
 * </ul>
 */
@Entity
public class Entry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(readOnly = true)
  private Long id;

  @Column(nullable = false)
  private LocalDateTime checkIn;

  @Column(nullable = false)
  private LocalDateTime checkOut;

  /**
   * Die Kategorie darf fehlen, deshalb ist die Spalte nullable.
   * Ohne {@code @JoinColumn} hiesse sie {@code category_id} ohnehin — die
   * Annotation macht es nur ausdrücklich.
   */
  @ManyToOne
  @JoinColumn(name = "category_id")
  private Category category;

  /**
   * {@code @JoinTable} beschreibt die Zwischentabelle: {@code joinColumns}
   * zeigt auf diese Seite, {@code inverseJoinColumns} auf die Gegenseite.
   *
   * <p>Eager geladen, weil die Tags bei jeder Ausgabe eines Eintrags gebraucht
   * werden. Ohne das käme es beim Abbilden ins DTO zu einer
   * {@code LazyInitializationException}, sobald die Transaktion bereits
   * beendet ist.</p>
   */
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "entry_tag",
      joinColumns = @JoinColumn(name = "entry_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private Set<Tag> tags = new HashSet<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDateTime getCheckIn() {
    return checkIn;
  }

  public void setCheckIn(LocalDateTime checkIn) {
    this.checkIn = checkIn;
  }

  public LocalDateTime getCheckOut() {
    return checkOut;
  }

  public void setCheckOut(LocalDateTime checkOut) {
    this.checkOut = checkOut;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public Set<Tag> getTags() {
    return tags;
  }

  public void setTags(Set<Tag> tags) {
    this.tags = tags;
  }
}

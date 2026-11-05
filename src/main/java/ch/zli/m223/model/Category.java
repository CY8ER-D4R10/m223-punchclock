package ch.zli.m223.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Eine Kategorie, der mehrere Zeiteinträge zugeordnet sein können.
 *
 * Beziehung zu {@link Entry}: <b>1:n</b>. Der Fremdschlüssel liegt auf der
 * n-Seite, also in der Tabelle {@code entry} als Spalte {@code category_id}.
 * Diese Seite trägt deshalb {@code mappedBy} und ist die <i>Gegenseite</i> —
 * sie besitzt die Beziehung nicht.
 */
@Entity
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(readOnly = true)
  private Long id;

  @Column(nullable = false)
  private String title;

  /**
   * Gegenseite der Beziehung. Wird standardmässig <b>lazy</b> geladen (ToMany),
   * und dient hier nur dazu, beim Löschen zu prüfen, ob die Kategorie noch
   * verwendet wird.
   */
  @OneToMany(mappedBy = "category")
  private Set<Entry> entries = new HashSet<>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Set<Entry> getEntries() {
    return entries;
  }

  public void setEntries(Set<Entry> entries) {
    this.entries = entries;
  }
}

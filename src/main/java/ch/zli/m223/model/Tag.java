package ch.zli.m223.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Ein Schlagwort, das mehreren Zeiteinträgen zugeordnet sein kann.
 *
 * Beziehung zu {@link Entry}: <b>n:m</b>. Sie wird über die Zwischentabelle
 * {@code entry_tag} abgebildet. Die besitzende Seite ist {@link Entry} — dort
 * steht {@code @JoinTable}. Diese Seite trägt deshalb {@code mappedBy}.
 */
@Entity
public class Tag {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(readOnly = true)
  private Long id;

  @Column(nullable = false)
  private String title;

  /** Gegenseite der n:m-Beziehung; standardmässig lazy. */
  @ManyToMany(mappedBy = "tags")
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

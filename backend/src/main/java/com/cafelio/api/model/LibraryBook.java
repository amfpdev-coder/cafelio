package com.cafelio.api.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "library_books",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_library_book_user_open_library",
                        columnNames = {"user_id", "open_library_id"}
                )
        }
)
public class LibraryBook {

    public LibraryBook(){

    }

    public LibraryBook(User user, String openLibraryId, String title, Integer firstPublishYear, String coverUrl, ReadingStatus status, List<String> authors, Set<BookTag> tags){
        this.user = user;
        this.openLibraryId = openLibraryId;
        this.title = title;
        this.firstPublishYear = firstPublishYear;
        this.coverUrl = coverUrl;
        this.status = status;
        this.authors = authors;
        this.tags = tags;
    }

    public UUID getId() {
        return id;
    }

    public String getOpenLibraryId(){
        return openLibraryId;
    }

    public String getTitle(){
        return title;
    }

    public Integer getFirstPublishYear(){
        return firstPublishYear;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public ReadingStatus getStatus() {
        return status;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public Set<BookTag> getTags() {
        return tags;
    }

    public void setStatus(ReadingStatus status){
        this.status = status;
    }

    public void setTags(Set<BookTag> tags) {
        this.tags = tags;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "open_library_id", nullable = false)
    private String openLibraryId;

    @Column(nullable = false)
    private String title;

    @Column(name = "first_publish_year")
    private Integer firstPublishYear;

    @Column(name = "cover_url")
    private String coverUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReadingStatus status;

    @ElementCollection
    @CollectionTable(
            name = "library_book_authors",
            joinColumns = @JoinColumn(name = "library_book_id")
    )
    @Column(name = "author", nullable = false)
    private List<String> authors = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "library_book_tags",
            joinColumns = @JoinColumn(name = "library_book_id")
    )
    @Column(name = "tag", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<BookTag> tags = new HashSet<>();
}
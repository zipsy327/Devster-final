package data.repository;

import data.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity,Integer> {

    public void deleteById(int rb_idx);

}




import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EcudTrctorOpertRepository extends JpaRepository<EcudTrctorOpert, Long> {

    @Query("SELECT DISTINCT e " +
           "FROM EcudTrctorOpert e " +
           "JOIN EcumFmwrkSchdul f ON e.fmwrkSn = f.fmwrkSn " +
           "WHERE e.trctorId = :trctorId")
    Page<EcudTrctorOpert> findByTrctorId(@Param("trctorId") String trctorId, Pageable pageable);
}

package az.demo.NexoraAcademy.repository.crm;

import az.demo.NexoraAcademy.entity.crm.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CampaignRepository extends JpaRepository<Campaign, UUID> {
}

package az.demo.NexoraAcademy.service.crm;

import az.demo.NexoraAcademy.dto.crm.CampaignRequest;
import az.demo.NexoraAcademy.dto.crm.CampaignResponse;
import az.demo.NexoraAcademy.entity.crm.Campaign;
import az.demo.NexoraAcademy.exception.ResourceNotFoundException;
import az.demo.NexoraAcademy.repository.crm.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CampaignService {

    private final CampaignRepository campaignRepository;

    @Transactional(readOnly = true)
    public List<CampaignResponse> findAll() {
        return campaignRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CampaignResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public CampaignResponse create(CampaignRequest request) {
        Campaign campaign = new Campaign();
        applyFields(campaign, request);
        return toResponse(campaignRepository.saveAndFlush(campaign));
    }

    public CampaignResponse update(UUID id, CampaignRequest request) {
        Campaign campaign = getOrThrow(id);
        applyFields(campaign, request);
        return toResponse(campaignRepository.saveAndFlush(campaign));
    }

    public CampaignResponse patch(UUID id, CampaignRequest request) {
        Campaign campaign = getOrThrow(id);

        if (request.name() != null) campaign.setName(request.name());
        if (request.bannerImageUrl() != null) campaign.setBannerImageUrl(request.bannerImageUrl());
        if (request.ctaUrl() != null) campaign.setCtaUrl(request.ctaUrl());
        if (request.discountPct() != null) campaign.setDiscountPct(request.discountPct());
        if (request.startsAt() != null) campaign.setStartsAt(request.startsAt());
        if (request.endsAt() != null) campaign.setEndsAt(request.endsAt());
        if (request.active() != null) campaign.setActive(request.active());
        if (request.priority() != null) campaign.setPriority(request.priority());
        if (request.courseIds() != null) campaign.setCourseIds(request.courseIds());

        return toResponse(campaignRepository.saveAndFlush(campaign));
    }

    public void delete(UUID id) {
        if (!campaignRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Campaign", id);
        }
        campaignRepository.deleteById(id);
    }

    private void applyFields(Campaign campaign, CampaignRequest request) {
        campaign.setName(request.name());
        campaign.setBannerImageUrl(request.bannerImageUrl());
        campaign.setCtaUrl(request.ctaUrl());
        campaign.setDiscountPct(request.discountPct());
        campaign.setStartsAt(request.startsAt());
        campaign.setEndsAt(request.endsAt());
        campaign.setActive(request.active() != null ? request.active() : true);
        campaign.setPriority(request.priority() != null ? request.priority() : 0);
        campaign.setCourseIds(request.courseIds() != null ? request.courseIds() : new UUID[0]);
    }

    private Campaign getOrThrow(UUID id) {
        return campaignRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Campaign", id));
    }

    private CampaignResponse toResponse(Campaign campaign) {
        return new CampaignResponse(
                campaign.getId(),
                campaign.getName(),
                campaign.getBannerImageUrl(),
                campaign.getCtaUrl(),
                campaign.getDiscountPct(),
                campaign.getStartsAt(),
                campaign.getEndsAt(),
                campaign.getActive(),
                campaign.getPriority(),
                campaign.getCourseIds()
        );
    }
}

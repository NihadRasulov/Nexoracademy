using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[ApiController]
[Route("api/courses")]
[Authorize(Roles = Roles.ContentManager)]
public class CourseController(ICourseApiClient client) : ControllerBase
{
    [HttpGet]
    public async Task<ActionResult<BffC.PagedResult<BffC.CourseResponse>>> List(
        [FromQuery] string? q, [FromQuery] short? categoryId, [FromQuery] string? difficulty,
        [FromQuery] string? deliveryFormat, [FromQuery] bool? published, [FromQuery] bool? active,
        [FromQuery] int? page, [FromQuery] int? size, [FromQuery] string? sort, CancellationToken ct)
    {
        var result = await client.ListAsync(q, categoryId, difficulty, deliveryFormat, published, active, page, size, sort, ct);
        return Ok(new BffC.PagedResult<BffC.CourseResponse>(
            result.Content.Select(ToBff).ToList(), result.TotalElements, result.TotalPages, result.Number, result.Size));
    }

    [HttpGet("{id:guid}")]
    public async Task<ActionResult<BffC.CourseResponse>> Get(Guid id, CancellationToken ct)
    {
        var course = await client.GetAsync(id, ct);
        return Ok(ToBff(course));
    }

    [HttpPost]
    public async Task<ActionResult<BffC.CourseResponse>> Create([FromBody] BffC.CourseRequest request, CancellationToken ct)
    {
        var created = await client.CreateAsync(ToBackend(request), ct);
        return StatusCode(StatusCodes.Status201Created, ToBff(created));
    }

    [HttpPut("{id:guid}")]
    public async Task<ActionResult<BffC.CourseResponse>> Replace(Guid id, [FromBody] BffC.CourseRequest request, CancellationToken ct)
    {
        var updated = await client.ReplaceAsync(id, ToBackend(request), ct);
        return Ok(ToBff(updated));
    }

    [HttpPatch("{id:guid}")]
    public async Task<ActionResult<BffC.CourseResponse>> Patch(Guid id, [FromBody] BffC.CourseRequest request, CancellationToken ct)
    {
        var updated = await client.PatchAsync(id, ToBackend(request), ct);
        return Ok(ToBff(updated));
    }

    [HttpDelete("{id:guid}")]
    public async Task<IActionResult> Delete(Guid id, CancellationToken ct)
    {
        await client.DeleteAsync(id, ct);
        return NoContent();
    }

    private static BffC.CourseResponse ToBff(BackendC.CourseResponse r) => new(
        r.Id, r.Slug, r.CategoryId, r.Title, r.ShortDescription, r.FullDescription, r.TargetAudience,
        r.Difficulty, r.DurationWeeks, r.DeliveryFormat, r.LocationText, r.BasePrice, r.Currency,
        r.PricePeriod, r.Published, r.Active, r.Archived, r.ValidFrom, r.ValidUntil, r.Content,
        r.RelatedCourseIds, r.CreatedBy, r.CreatedAt, r.UpdatedAt);

    private static BackendC.CourseRequest ToBackend(BffC.CourseRequest r) => new(
        r.Slug, r.CategoryId, r.Title, r.ShortDescription, r.FullDescription, r.TargetAudience,
        r.Difficulty, r.DurationWeeks, r.DeliveryFormat, r.LocationText, r.BasePrice, r.Currency,
        r.PricePeriod, r.Published, r.Active, r.Archived, r.ValidFrom, r.ValidUntil, r.Content,
        r.RelatedCourseIds);
}

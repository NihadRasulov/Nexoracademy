using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Auth;
using NexoraAcademy.AdminBff.Clients;
using BackendC = NexoraAcademy.AdminBff.Contracts.Backend;
using BffC = NexoraAcademy.AdminBff.Contracts.Bff;

namespace NexoraAcademy.AdminBff.Controllers;

[ApiController]
[Route("api/course-instructors")]
[Authorize(Roles = Roles.ContentManager)]
public class CourseInstructorController(ICourseInstructorApiClient client) : ControllerBase
{
    [HttpGet]
    public async Task<ActionResult<List<BffC.CourseInstructorResponse>>> List(CancellationToken ct)
    {
        var items = await client.ListAsync(ct);
        return Ok(items.Select(ToBff).ToList());
    }

    [HttpGet("{courseId:guid}/{instructorId:guid}")]
    public async Task<ActionResult<BffC.CourseInstructorResponse>> Get(Guid courseId, Guid instructorId, CancellationToken ct)
    {
        var item = await client.GetAsync(courseId, instructorId, ct);
        return Ok(ToBff(item));
    }

    [HttpPost]
    public async Task<ActionResult<BffC.CourseInstructorResponse>> Create(
        [FromBody] BffC.CourseInstructorRequest request, CancellationToken ct)
    {
        var created = await client.CreateAsync(ToBackend(request), ct);
        return StatusCode(StatusCodes.Status201Created, ToBff(created));
    }

    [HttpPut("{courseId:guid}/{instructorId:guid}")]
    public async Task<ActionResult<BffC.CourseInstructorResponse>> Replace(
        Guid courseId, Guid instructorId, [FromBody] BffC.CourseInstructorRequest request, CancellationToken ct)
    {
        var updated = await client.ReplaceAsync(courseId, instructorId, ToBackend(request), ct);
        return Ok(ToBff(updated));
    }

    [HttpPatch("{courseId:guid}/{instructorId:guid}")]
    public async Task<ActionResult<BffC.CourseInstructorResponse>> Patch(
        Guid courseId, Guid instructorId, [FromBody] BffC.CourseInstructorRequest request, CancellationToken ct)
    {
        var updated = await client.PatchAsync(courseId, instructorId, ToBackend(request), ct);
        return Ok(ToBff(updated));
    }

    [HttpDelete("{courseId:guid}/{instructorId:guid}")]
    public async Task<IActionResult> Delete(Guid courseId, Guid instructorId, CancellationToken ct)
    {
        await client.DeleteAsync(courseId, instructorId, ct);
        return NoContent();
    }

    private static BffC.CourseInstructorResponse ToBff(BackendC.CourseInstructorResponse r) =>
        new(r.CourseId, r.InstructorId, r.Role);

    private static BackendC.CourseInstructorRequest ToBackend(BffC.CourseInstructorRequest r) =>
        new(r.CourseId, r.InstructorId, r.Role);
}

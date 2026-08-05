using Microsoft.AspNetCore.Mvc;
using NexoraAcademy.AdminBff.Clients.Generic;

namespace NexoraAcademy.AdminBff.Controllers.Generic;

[ApiController]
public abstract class BffCrudControllerBase<TBackendResponse, TBackendRequest, TBffResponse, TBffRequest, TId>
    : ControllerBase
{
    protected abstract IBackendCrudClient<TBackendResponse, TBackendRequest, TId> Client { get; }
    protected abstract TBffResponse ToBff(TBackendResponse response);
    protected abstract TBackendRequest ToBackend(TBffRequest request);

    [HttpGet]
    public virtual async Task<ActionResult<List<TBffResponse>>> List(CancellationToken ct)
    {
        var items = await Client.ListAsync(ct);
        return Ok(items.Select(ToBff).ToList());
    }

    [HttpGet("{id}")]
    public virtual async Task<ActionResult<TBffResponse>> Get(TId id, CancellationToken ct)
    {
        var item = await Client.GetAsync(id, ct);
        return Ok(ToBff(item));
    }

    [HttpPost]
    public virtual async Task<ActionResult<TBffResponse>> Create([FromBody] TBffRequest request, CancellationToken ct)
    {
        var created = await Client.CreateAsync(ToBackend(request), ct);
        return StatusCode(StatusCodes.Status201Created, ToBff(created));
    }

    [HttpPut("{id}")]
    public virtual async Task<ActionResult<TBffResponse>> Replace(TId id, [FromBody] TBffRequest request, CancellationToken ct)
    {
        var updated = await Client.ReplaceAsync(id, ToBackend(request), ct);
        return Ok(ToBff(updated));
    }

    [HttpPatch("{id}")]
    public virtual async Task<ActionResult<TBffResponse>> Patch(TId id, [FromBody] TBffRequest request, CancellationToken ct)
    {
        var updated = await Client.PatchAsync(id, ToBackend(request), ct);
        return Ok(ToBff(updated));
    }

    [HttpDelete("{id}")]
    public virtual async Task<IActionResult> Delete(TId id, CancellationToken ct)
    {
        await Client.DeleteAsync(id, ct);
        return NoContent();
    }
}

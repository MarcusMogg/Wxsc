using System.Collections.Generic;
using System.Linq;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Wxsc.Models;


namespace Wxsc.Controllers
{
    [Authorize]
    [Route("api/[controller]")]
    public class MenuController : Controller
    {
        private readonly MDbContext _context;
        public MenuController(MDbContext context)
        {
            _context = context;
        }
        // GET: api/<controller>
        [Authorize]
        [HttpGet]
        public ActionResult<List<MenuItem>> Get()
        {
            return _context.MenuItems.ToList();
        }

        // GET api/<controller>/name
        [Authorize]
        [HttpGet("{name}")]
        public ActionResult<MenuItem> Get(string name)
        {
            var items = _context.MenuItems;
            var item = from i in items where i.Name == name select i;
            if (!item.Any())
            {
                return NotFound();
            }
            return item.First();
        }

    }
}

using System.Collections.Generic;
using System.Linq;
using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Wxsc.Models;

namespace Wxsc.Controllers
{
    [Authorize]
    [Route("api/[controller]")]
    public class FavFoodController : Controller
    {
        private readonly MDbContext _context;

        public FavFoodController(MDbContext context)
        {
            _context = context;
        }

        [HttpGet]
        public ActionResult<List<MenuItem>> Get()
        {
            string userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            var l = _context.FavFoods.Where(a => a.UserId == userId).
                Select(a => a.MenuItemId).
                ToList();
            var res = _context.MenuItems.Where(a => l.Contains(a.Id)).ToList();
            return res;
        }

        [HttpDelete]
        public ActionResult Del(string name)
        {
            string userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (string.IsNullOrEmpty(userId))
            {
                return Ok(new
                {
                    Request = new Result(1, "Authorize error")

                });
            }
            var t = _context.MenuItems.FirstOrDefault(a => a.Name == name);
            if (t == null)
            {
                return Ok(new
                {
                    Request = new Result(2, "Food name error")

                });
            }
            var i = t.Id;
            var l = _context.FavFoods.FirstOrDefault(a => a.UserId == userId && a.MenuItemId == i);
            if (l == null)
            {
                return Ok(new
                {
                    Request = new Result(2, "Food name error")

                });
            }
            _context.FavFoods.Remove(l);
            _context.SaveChanges();
            return Ok(new
            {
                Request = new Result(0, "Success")
            });
        }

        [HttpPost]
        public ActionResult Add(string name)
        {
            string userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            FavFood f = new FavFood();
            f.UserId = userId;
            var t = _context.MenuItems.FirstOrDefault(a => a.Name == name);
            if (string.IsNullOrEmpty(userId))
            {
                return Ok(new
                {
                    Request = new Result(1, "Authorize error")

                });
            }
            if (t == null)
            {
                return Ok(new
                {
                    Request = new Result(2, "Food name error")
                });
            }
            f.MenuItemId = t.Id;
            _context.FavFoods.Add(f);
            _context.SaveChanges();
            return Ok(new
            {
                Request = new Result(0, "Success")
            });
        }
    }
}
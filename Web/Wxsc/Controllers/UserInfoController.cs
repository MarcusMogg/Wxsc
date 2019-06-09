using System;
using System.Linq;
using System.Security.Claims;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Wxsc.Models;

namespace Wxsc.Controllers
{
    [Authorize]
    [Route("api/[controller]/[action]")]
    public class UserInfoController : Controller
    {
        private readonly UserManager<User> _user;

        public UserInfoController(UserManager<User> user)
        {
            _user = user;
        }

        
        [HttpGet]
        public async Task<IActionResult> GetAll()
        {
            string userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (String.IsNullOrEmpty(userId))
            {
                return Ok(new
                {
                    Request = new Result(1, "Authorize error")

                });
            }
            var user = await _user.FindByIdAsync(userId);
            if (user == null)
            {
                return Ok(new
                {
                    Request = new Result(1, "Authorize error")

                });
            }
            return Ok(new
            {
                Request = new Result(0, "Success"),
                User = new
                {
                    user.NickName,
                    user.Age,
                    user.Sex,
                    user.High,
                    user.Weight,
                    user.IsHighSugar,
                    user.IsHighFat,
                }
            });
        }

        
        [HttpPost]
        public async Task<IActionResult> SetAll(UserViewModel model)
        {
            if (ModelState.IsValid)
            {
            }

            string userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
            if (String.IsNullOrEmpty(userId))
            {
                return Ok(new
                {
                    Request = new Result(1, "Authorize error")

                });
            }
            var user = await _user.FindByIdAsync(userId);
            if (user == null)
            {
                return Ok(new
                {
                    Request = new Result(1, "Authorize error")

                });
            }

            try
            {
                user.NickName = String.IsNullOrEmpty(model.NickName) ? user.NickName : model.NickName;
                user.Age = int.Parse(model.Age);
                user.Sex = int.Parse(model.Sex);
                user.High = double.Parse(model.High);
                user.Weight = double.Parse(model.Weight);
                user.IsHighSugar = int.Parse(model.IsHighSugar);
                user.IsHighFat = int.Parse(model.IsHighFat);
            }
            catch (Exception e)
            {
                return Ok(new { Request = new Result(1, "参数错误") });
            }
            var result = await _user.UpdateAsync(user);
            if (result.Succeeded)
            {
                return Ok(new { Request = new Result(0, "Success") });
            }
            AddErrors(result);

            string messages = string.Join("; ", ModelState.Values.SelectMany(x => x.Errors).Select(x => x.ErrorMessage));
            return Ok(new
            {
                Request = new Result(1, messages)
            });
        }

        private void AddErrors(IdentityResult result)
        {
            foreach (var error in result.Errors)
            {
                ModelState.AddModelError(string.Empty, error.Description);
            }
        }
    }
}